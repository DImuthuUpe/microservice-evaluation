package io.upeksha.helix;

import org.apache.helix.HelixManager;
import org.apache.helix.controller.rebalancer.Rebalancer;
import org.apache.helix.controller.stages.ClusterDataCache;
import org.apache.helix.controller.stages.CurrentStateOutput;
import org.apache.helix.model.IdealState;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class AffinityRebalancer implements Rebalancer {

    private List<String> affinityInstances = new ArrayList<>();

    @Override
    public void init(HelixManager manager) {
        affinityInstances.add("node-1");
        affinityInstances.add("node-2");
    }

    @Override
    public IdealState computeNewIdealState(String resourceName, IdealState currentIdealState, CurrentStateOutput currentStateOutput, ClusterDataCache clusterData) {

        List<String> liveParticipants = new ArrayList<String>(clusterData.getLiveInstances().keySet());
        int lockHolders = Integer.parseInt(currentIdealState.getReplicas());

        for (String partition : currentIdealState.getPartitionSet()) {
            List<String> preferenceList = new ArrayList<String>();

            // find for affinity instances in participants
            boolean foundAffinity = false;
            for (String participant : liveParticipants) {
                for (String affinityInstance : affinityInstances) {
                    if (participant.equals(affinityInstance)) {
                        preferenceList.add(affinityInstance);
                        foundAffinity = true;
                    }
                }
            }

            if (!foundAffinity) {
                // if there is no affinity instance alive, use any instance
                preferenceList = liveParticipants;
            }
            currentIdealState.setPreferenceList(partition, preferenceList);
        }
        return currentIdealState;
    }
}
