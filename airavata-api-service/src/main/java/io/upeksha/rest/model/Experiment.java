package io.upeksha.rest.model;

import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 17.07
 */
public class Experiment {

    private String experimentId;
    private String projectId;
    private String gatewayId;
    private List<String> experimentInputs;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public List<String> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<String> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }
}
