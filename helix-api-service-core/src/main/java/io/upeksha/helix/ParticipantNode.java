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
    private String participantName;
    private HelixManager helixManager;

    public ParticipantNode(String zkAddress, String clusterName, String participantName) {
        System.out.println("Initializing Participant Node");
        this.zkAddress = zkAddress;
        this.clusterName = clusterName;
        this.participantName = participantName;
    }

    @Override
    public void run() {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            List<String> nodesInCluster = zkHelixAdmin.getInstancesInCluster(clusterName);
            if (!nodesInCluster.contains(participantName)) {
                InstanceConfig instanceConfig = new InstanceConfig(participantName);
                instanceConfig.setHostName("localhost");
                instanceConfig.setInstanceEnabled(true);
                zkHelixAdmin.addInstance(clusterName, instanceConfig);
                System.out.println("Instance: " + participantName + ", has been added to cluster: " + clusterName);
            }

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        @Override
                        public void run() {
                            System.out.println("Participant: " + participantName + ", shutdown hook called.");
                            disconnect();
                        }
                    }
            );

            // connect the participant manager
            connect();
        } catch (Exception ex) {
            System.out.println("Error in run() for Participant: " + participantName + ", reason: " + ex);
            ex.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }

    }

    public void connect() {
        helixManager = HelixManagerFactory.getZKHelixManager(clusterName, participantName, InstanceType.PARTICIPANT, zkAddress);
        StateMachineEngine stateMachineEngine = helixManager.getStateMachineEngine();
        OnlineOfflineStateModelFactory stateModelFactory = new OnlineOfflineStateModelFactory();
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
            System.out.println("Participant: " + participantName + ", has disconnected from cluster: " + clusterName);
            helixManager.disconnect();
        }
    }

    public static void main(String args[]) {
        ParticipantNode node = new ParticipantNode("localhost:2199", "MicroServices", "node3"); // node1, node2, node3
        new Thread(node).start();
    }
}
