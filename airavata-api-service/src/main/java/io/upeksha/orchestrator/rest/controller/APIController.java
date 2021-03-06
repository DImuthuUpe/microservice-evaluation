package io.upeksha.orchestrator.rest.controller;

import io.upeksha.orchestrator.rest.model.Experiment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */

@RestController
public class APIController {

    @RequestMapping(method = RequestMethod.POST, value = "/experiment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String submitExperiment(@RequestBody Experiment experiment) throws UnknownHostException {
        return "Experiment " + experiment.getExperimentId() + " was accepted by " + InetAddress.getLocalHost().getHostName();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/exit")
    public String exit() throws UnknownHostException {
        System.out.println("Exiting the system");
        System.exit(0);
        return "success";
    }

}
