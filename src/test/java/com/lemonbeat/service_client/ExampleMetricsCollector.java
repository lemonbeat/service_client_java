package com.lemonbeat.service_client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MetricsCollector;

/**
 * This is just a very simple example how a custom MetricsCollector is implemented.
 * Please have a look at the documentation here for other examples:
 * https://www.rabbitmq.com/api-guide.html#metrics
 */
public class ExampleMetricsCollector implements MetricsCollector {

    public int connectionCount = 0;
    public int channelCount = 0;
    public int basicPublishCount = 0;
    public int basicPublishFailureCount = 0;
    public int basicPublishAckCount = 0;
    public int basicPublishNackCount = 0;
    public int basicPublishUnroutedCount = 0;
    public int consumedMessageCount = 0;
    public int basicAckCount = 0;
    public int basicNackCount = 0;
    public int basicRejectCount = 0;
    public int basicConsumeCount = 0;
    public int basicCancelCount = 0;

    @Override
    public void newConnection(Connection connection) {
        this.connectionCount++;
    }

    @Override
    public void closeConnection(Connection connection) {
        this.connectionCount--;
    }

    @Override
    public void newChannel(Channel channel) {
        this.channelCount++;
    }

    @Override
    public void closeChannel(Channel channel) {
        this.channelCount--;
    }

    @Override
    public void basicPublish(Channel channel) {
        this.basicPublishCount++;
    }

    @Override
    public void basicPublishFailure(Channel channel, Throwable cause) {
        this.basicPublishFailureCount++;
    }

    @Override
    public void basicPublishAck(Channel channel, long deliveryTag, boolean multiple) {
        this.basicPublishAckCount++;
    }

    @Override
    public void basicPublishNack(Channel channel, long deliveryTag, boolean multiple) {
        this.basicPublishNackCount++;
    }

    @Override
    public void basicPublishUnrouted(Channel channel) {
        this.basicPublishUnroutedCount++;
    }

    @Override
    public void consumedMessage(Channel channel, long deliveryTag, boolean autoAck) {
        this.consumedMessageCount++;
    }

    @Override
    public void consumedMessage(Channel channel, long deliveryTag, String consumerTag) {
        this.consumedMessageCount++;
    }

    @Override
    public void basicAck(Channel channel, long deliveryTag, boolean multiple) {
        this.basicAckCount++;
    }

    @Override
    public void basicNack(Channel channel, long deliveryTag) {
        this.basicNackCount++;
    }

    @Override
    public void basicReject(Channel channel, long deliveryTag) {
        this.basicRejectCount++;
    }

    @Override
    public void basicConsume(Channel channel, String consumerTag, boolean autoAck) {
        this.basicConsumeCount++;
    }

    @Override
    public void basicCancel(Channel channel, String consumerTag) {
        this.basicCancelCount++;
    }
}
