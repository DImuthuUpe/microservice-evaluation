package io.upeksha.orchestrator.rest;

import io.upeksha.orchestrator.rest.elector.ElectionListener;
import io.upeksha.orchestrator.rest.elector.ElectionMonitor;
import io.upeksha.orchestrator.rest.elector.OrchestratorCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */

@SpringBootApplication(scanBasePackages={"io.upeksha.orchestrator.rest"})
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String args[]) {

        ElectionMonitor electionMonitor = new ElectionMonitor();
        ElectionListener electionListener = new OrchestratorCore();
        electionMonitor.addListener(electionListener);
        electionMonitor.start();

        SpringApplication.run(Application.class, args);
    }
}
