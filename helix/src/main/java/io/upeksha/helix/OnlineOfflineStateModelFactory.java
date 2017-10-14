package io.upeksha.helix;

import org.apache.helix.NotificationContext;
import org.apache.helix.model.Message;
import org.apache.helix.participant.statemachine.StateModel;
import org.apache.helix.participant.statemachine.StateModelFactory;
import org.apache.helix.participant.statemachine.StateModelInfo;
import org.apache.helix.participant.statemachine.Transition;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class OnlineOfflineStateModelFactory extends StateModelFactory<StateModel> {

    final ExecutorService exService = Executors.newSingleThreadExecutor();
    private Server server;
    private int serverBasePort = 8080;

    public OnlineOfflineStateModelFactory(int nodeIndex) {
        init(nodeIndex);
    }

    public void init(int nodeIndex) {

        // We start an embedded jetty to launch the web application that contains microservice functions
        this.server = new Server(serverBasePort + nodeIndex);
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("/opt/service.war");
        webapp.setExtractWAR(true);
        webapp.setCopyWebInf(true);

        Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration");

        webapp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*//*[^/]*servlet-api-[^/]*\\.jar$|.*//*javax.servlet.jsp.jstl-.*\\.jar$|.*//*[^/]*taglibs.*\\.jar$" );

        server.setHandler(webapp);
        server.dumpStdErr();
    }

     @Override
    public StateModel createNewStateModel(String resourceName, String partitionName) {
        OnlineOfflineStateModel stateModel = new OnlineOfflineStateModel();
        return stateModel;
    }

    @StateModelInfo(states = "{'OFFLINE','ONLINE'}", initialState = "OFFLINE")
    public class OnlineOfflineStateModel extends StateModel {

        @Transition(from = "OFFLINE", to = "ONLINE")
        public void onBecomeOnlineFromOffline(Message message,
                                              NotificationContext context) {
            System.out.println("OnlineOfflineStateModel.onBecomeOnlineFromOffline()");

            if (server != null) {
                exService.submit(() -> {
                    try {
                        server.start();
                        System.out.println("Starting the server");
                        server.join();
                        System.out.println("Server stop has been triggered");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                throw new RuntimeException("Web server has not being initialized successfully");
            }
        }

        @Transition(from = "ONLINE", to = "OFFLINE")
        public void onBecomeOfflineFromOnline(Message message,
                                              NotificationContext context) throws Exception {
            System.out.println("OnlineOfflineStateModel.onBecomeOfflineFromOnline()");

            if (server == null || !server.isRunning()) {
                System.out.println("Web server is not running");
            } else {
                server.stop();
                System.out.println("Web server was stopped");
            }
        }
    }
}
