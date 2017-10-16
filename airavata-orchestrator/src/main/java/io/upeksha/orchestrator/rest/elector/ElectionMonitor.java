package io.upeksha.orchestrator.rest.elector;

import io.restassured.path.json.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
public class ElectionMonitor {

    private final List<ElectionListener> listeners = new ArrayList<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void addListener(ElectionListener electionListener) {
        this.listeners.add(electionListener);
    }

    public void start() {

        this.listeners.forEach(ElectionListener::start);

        executorService.scheduleAtFixedRate(() -> {
            String myPodName = System.getenv("HOSTNAME");
            String leaderPodName = null;
            try {

                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).build();
                HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
                HttpGet httpGet = new HttpGet("http://localhost:4040");
                HttpResponse response = httpClient.execute(httpGet);
                String payload = EntityUtils.toString(response.getEntity());

                leaderPodName = JsonPath.from(payload).getString("name");

                if (myPodName.equals(leaderPodName)) {
                    this.listeners.forEach(ElectionListener::onLeader);
                } else {
                    this.listeners.forEach(ElectionListener::onWorker);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.listeners.forEach(electionListener -> electionListener.onError(e));
            }

        }, 10, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        this.listeners.forEach(ElectionListener::stop);
    }


}
