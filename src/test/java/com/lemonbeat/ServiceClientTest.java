package com.lemonbeat;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ServiceClientTest {

    @Test
    public void subscribe() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        String consumerTag = serviceClient.subscribe("EVENT.APP.#", event -> {
            // TODO: Test an incoming event
            System.out.println(LsBL.write(event));
        });
        assertNotEquals("ConsumerTag for subscription", null, consumerTag);
    }

    @Test
    public void call() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        // Create a gw_list_get message
        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        // Send this message multiple times and assert answers
        for(int i = 0; i < 10; i++){
            serviceClient.call(lsbl, response -> {
                List<String> gwList = response.getResponse().getTopoResponse().getGwListGet().getGw();
                assertTrue(gwList.size() >= 0);
            });
        }
    }

}