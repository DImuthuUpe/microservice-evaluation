package io.upeksha.orchestrator.rest.elector;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public interface ElectionListener {

    public void start();
    public void onLeader();
    public void onWorker();
    public void onError(Exception e);
    public void stop();
}
