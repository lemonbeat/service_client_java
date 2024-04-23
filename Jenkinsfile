#!/usr/bin/env groovy

pipeline {

    agent {
        node {
            label 'swdev-docker'
        }
    }

    triggers {
        cron(env.BRANCH_NAME == 'master' ? 'H 7 * * *' : '')
    }

    options {
        gitLabConnection('gitlab.lemonbeat.com')
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '10'))
    }

    stages {

        stage('clean'){
            steps {
                updateGitlabCommitStatus name: 'service_client_java', state: 'running'
                cleanWs()
            }
        }

        stage('git pull'){
            steps {
                checkout([
                        $class: 'GitSCM',
                        branches: [[name: "${BRANCH_NAME}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        submoduleCfg: [],
                        userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }  // END stage git pull

        stage('prepare'){
            steps {
                updateGitlabCommitStatus name: 'service_client_java', state: 'running'
                sh '''
                docker network create service-client-net-${BUILD_ID}

                docker run -d \
                --name rabbit-${BUILD_ID} \
                --network service-client-net-${BUILD_ID} \
                -p 5672:5672 \
                -p 15672:15672 \
                -e RABBITMQ_DEFAULT_USER=user \
                -e RABBITMQ_DEFAULT_PASS=password \
                -e RABBITMQ_DEFAULT_VHOST=vhost \
                rabbitmq:3.13.1-management

                sleep 10

                docker exec rabbit-${BUILD_ID} rabbitmqadmin declare exchange -u user -p password --vhost=vhost name=PARTNER type=topic
                docker exec rabbit-${BUILD_ID} rabbitmqadmin declare exchange -u user -p password --vhost=vhost name=DMZ type=topic
                docker exec rabbit-${BUILD_ID} rabbitmqadmin declare exchange -u user -p password --vhost=vhost name=EVENT.APP type=topic

                sed -i "s/BROKER_HOST=localhost/BROKER_HOST=rabbit-${BUILD_ID}/g" settings.properties
                '''
            }
        } // END stage prepare


        stage('Test') {
            matrix {
                axes {
                    axis {
                        name 'JAVA_VERSION'
                        values '8', '11', '13', '17', '21'
                    }
                }
                stages {
                    stage("build and test") {
                        agent {
                            docker {
                                image "openjdk:${JAVA_VERSION}-jdk-buster"
                                label "swdev-docker"
                                reuseNode true
                                args '\
                                -u 0:0 \
                                --network service-client-net-${BUILD_ID} \
                                -v /var/run/docker.sock:/var/run/docker.sock'
                            }
                        }
                        steps {
                            sh script: """#!/bin/bash
                            ./gradlew test
                            """
                            junit 'build/test-results/**/*.xml'
                        }
                    }
                }
            }
        } // stage('Test')

        stage('docs and jar'){
            agent {
                docker {
                    image 'openjdk:21-jdk-buster'
                    label 'swdev-docker'
                    reuseNode true
                    args '\
                    -u 0:0 \
                    --network service-client-net-${BUILD_ID} \
                    -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            stages {
                stage('javadoc'){
                    steps {
                        sh '''
                        ./gradlew javadoc
                        '''
                        archiveArtifacts artifacts: 'build/docs/javadoc/**/*', allowEmptyArchive: 'true'
                    }
                }
                stage('jar'){
                    steps {
                        sh '''
                        ./gradlew jar
                        '''
                        archiveArtifacts artifacts: 'build/libs/*.jar', allowEmptyArchive: 'true'
                    }
                }
            }
        }

    } // END stages

    post {
        always {
            step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])])
            sh '''
            docker stop --time=1 rabbit-${BUILD_ID} || true
            docker rm -f rabbit-${BUILD_ID} || true
            docker network rm service-client-net-${BUILD_ID} || true
            sudo chown -R svc_jenkins:users ${PWD}
            '''
            cleanWs()
        }
        failure {
            updateGitlabCommitStatus name: 'service_client_java', state: 'failed'
            addGitLabMRComment comment: 'Well, that didn´t work obviously.'
        }
        success {
            updateGitlabCommitStatus name: 'service_client_java', state: 'success'
            addGitLabMRComment comment: 'This worked, as it should have.'
        }
        aborted {
            updateGitlabCommitStatus name: 'service_client_java', state: 'canceled'
            addGitLabMRComment comment: 'The build was canceled.'
        }

    } // END post

} // END pipeline