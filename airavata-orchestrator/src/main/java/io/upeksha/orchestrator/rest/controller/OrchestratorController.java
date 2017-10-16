package io.upeksha.orchestrator.rest.controller;

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
public class OrchestratorController {

    @RequestMapping(method = RequestMethod.GET, value = "/exit")
    public String exit() throws UnknownHostException {
        System.out.println("Exiting the system");
        System.exit(0);
        return "success";
    }

}
