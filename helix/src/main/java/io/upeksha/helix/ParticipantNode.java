package io.upeksha.helix;

import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.participant.StateMachineEngine;

import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class ParticipantNode implements Runnable {


    private String zkAddress;
    private String clusterName;
    private String participantNamePrefix = "node-";
    private HelixManager helixManager;
    private int nodeIndex;

    public ParticipantNode(String zkAddress, String clusterName, int nodeIndex) {
        System.out.println("Initializing Participant Node");
        this.zkAddress = zkAddress;
        this.clusterName = clusterName;
        this.nodeIndex = nodeIndex;
    }

    @Override
    public void run() {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            List<String> nodesInCluster = zkHelixAdmin.getInstancesInCluster(clusterName);
            if (!nodesInCluster.contains(participantNamePrefix + nodeIndex)) {
                InstanceConfig instanceConfig = new InstanceConfig(participantNamePrefix + nodeIndex);
                instanceConfig.setHostName("localhost");
                instanceConfig.setInstanceEnabled(true);
                zkHelixAdmin.addInstance(clusterName, instanceConfig);
                System.out.println("Instance: " + participantNamePrefix + nodeIndex + ", has been added to cluster: " + clusterName);
            }

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        @Override
                        public void run() {
                            System.out.println("Participant: " + participantNamePrefix + nodeIndex + ", shutdown hook called.");
                            disconnect();
                        }
                    }
            );

            // connect the participant manager
            connect();
        } catch (Exception ex) {
            System.out.println("Error in run() for Participant: " + participantNamePrefix + nodeIndex + ", reason: " + ex);
            ex.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }

    }

    public void connect() {
        helixManager = HelixManagerFactory.getZKHelixManager(clusterName, participantNamePrefix + nodeIndex,
                InstanceType.PARTICIPANT, zkAddress);
        StateMachineEngine stateMachineEngine = helixManager.getStateMachineEngine();
        OnlineOfflineStateModelFactory stateModelFactory = new OnlineOfflineStateModelFactory(nodeIndex);
        stateMachineEngine.registerStateModelFactory("OnlineOfflineStateModelDef", stateModelFactory);
        try {
            helixManager.connect();
            System.out.println("Helix participant successfully connected to cluster");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println("Helix participant failed to connect to cluster due to " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (helixManager != null) {
            System.out.println("Participant: " + participantNamePrefix + nodeIndex + ", has disconnected from cluster: " + clusterName);
            helixManager.disconnect();
        }
    }

    public static void main(String args[]) {
        ParticipantNode node = new ParticipantNode("localhost:2199", "MicroServices", 2); // 1, 2, 3
        new Thread(node).start();
    }
}
