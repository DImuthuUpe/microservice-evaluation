package io.upeksha.helix;

import org.apache.helix.HelixManager;
import org.apache.helix.controller.HelixControllerMain;

import java.util.concurrent.CountDownLatch;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class ControllerNode implements Runnable {

    private String clusterName;
    private String controllerName;
    private String zkAddress;
    private HelixManager zkHelixManager;

    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch stopLatch = new CountDownLatch(1);

    public ControllerNode(String zkAddress, String clusterName, String controllerName) {
        this.clusterName = clusterName;
        this.controllerName = controllerName;
        this.zkAddress = zkAddress;
    }

    @Override
    public void run() {
        try {
            zkHelixManager = HelixControllerMain.startHelixController(zkAddress, clusterName, controllerName,
                    HelixControllerMain.STANDALONE);
            startLatch.countDown();
            stopLatch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            disconnect();
        }

    }

    public void start() {
        new Thread(this).start();
        try {
            startLatch.await();
            System.out.println("Controller: " + controllerName + ", has connected to cluster: " + clusterName);
        } catch (InterruptedException ex) {
            System.out.println("Controller: " + controllerName + ", is interrupted! reason: " + ex);
            ex.printStackTrace();
        }

    }

    public void stop() {
        stopLatch.countDown();
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            System.out.println("Controller: " + controllerName + ", has disconnected from cluster: " + clusterName);
            zkHelixManager.disconnect();
        }
    }

    public static void main(String args[]) {
        ControllerNode controllerNode = new ControllerNode("localhost:2199", "MicroServices", "ControllerNode");
        controllerNode.start();
    }
}
