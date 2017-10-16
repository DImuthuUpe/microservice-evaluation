package io.upeksha.orchestrator.rest.elector;

import java.util.concurrent.*;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */

public class OrchestratorCore implements ElectionListener {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private enum Mode {LEADER, WORKER, UNKNOWN};
    private Mode currentMode = Mode.WORKER;

    private boolean checkIfSameMode(Mode mode) {
        return mode == currentMode;
    }

    public void onLeader() {
        if (checkIfSameMode(Mode.LEADER)) return;
        System.out.println("Orchestrator mode changed from " + currentMode.name() + " to " + Mode.LEADER);
        currentMode = Mode.LEADER;
    }

    public void onWorker() {
        if (checkIfSameMode(Mode.WORKER)) return;
        System.out.println("Orchestrator mode changed from " + currentMode.name() + " to " + Mode.WORKER);
        currentMode = Mode.WORKER;
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Orchestrator mode changed from " + currentMode.name() + " to " + Mode.UNKNOWN +
                " due to " + e.getMessage());
        currentMode = Mode.UNKNOWN;
    }

    @Override
    public void stop() {
        // TODO
    }

    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            if (currentMode == Mode.WORKER) {
                System.out.println("Orchestrator now runs in worker mode");
            } else if (currentMode == Mode.LEADER) {
                System.out.println("Orchestrator now runs in leader mode");
            } else if (currentMode == Mode.UNKNOWN) {
                System.out.println("Orchestrator now runs in unknown mode");
            }
        }, 10, 2, TimeUnit.SECONDS);
    }
}
