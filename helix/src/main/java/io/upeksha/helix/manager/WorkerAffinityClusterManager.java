package io.upeksha.helix.manager;

import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.StateModelDefinition;

import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class WorkerAffinityClusterManager {
    private String zkServers = "localhost:2199";
    private String clusterName = "MicroServices";
    private String stateModelDef = "OnlineOfflineStateModelDef";
    private String resourceName = "API-Service";
    private int partitions = 1;
    private int replicas = 2;

    public void initCluster() {
        ZkClient zkClient = new ZkClient(zkServers, ZkClient.DEFAULT_SESSION_TIMEOUT,
                ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
        ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

        zkHelixAdmin.addCluster(clusterName, true);

        StateModelDefinition.Builder builder = new StateModelDefinition.Builder(stateModelDef);
        builder.addState("ONLINE", 1);
        builder.addState("OFFLINE", 2);
        builder.addState("DROPPED");

        builder.initialState("OFFLINE");

        builder.addTransition("ONLINE", "OFFLINE");
        builder.addTransition("OFFLINE", "ONLINE");

        builder.addTransition("ONLINE", "DROPPED");
        builder.addTransition("OFFLINE", "DROPPED");

        builder.dynamicUpperBound("ONLINE", "R");

        StateModelDefinition definition = builder.build();
        zkHelixAdmin.addStateModelDef(clusterName, stateModelDef, definition);

        while (true) {
            System.out.println("Waiting for at least " + (replicas + 1) + " instances to be added to the cluster");
            List<String> instances = zkHelixAdmin.getInstancesInCluster(clusterName);
            System.out.println("Current instances : " + String.join(",", instances));
            if (instances.size() >= (replicas + 1)) {
                System.out.println("Required instances are added. Configuring resources");

                zkHelixAdmin.addResource(clusterName, resourceName, partitions, stateModelDef);

                IdealState idealState = zkHelixAdmin.getResourceIdealState(clusterName, resourceName);
                idealState.setRebalanceMode(IdealState.RebalanceMode.USER_DEFINED);
                idealState.setRebalancerClassName("io.upeksha.helix.AffinityRebalancer");
                zkHelixAdmin.setResourceIdealState(clusterName, resourceName, idealState);

                zkHelixAdmin.rebalance(clusterName, resourceName, replicas);
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Successfully configured the cluster " + clusterName);
    }

    public static void main(String args[]) {
        AvailabilityClusterManager clusterManager = new AvailabilityClusterManager();
        clusterManager.initCluster();
    }
}
