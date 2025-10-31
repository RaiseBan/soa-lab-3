package com.musicband.grammy.client;

import com.musicband.grammy.consul.ConsulServiceDiscovery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import java.util.logging.Logger;

@ApplicationScoped
public class MainApiClient {

    private static final Logger LOGGER = Logger.getLogger(MainApiClient.class.getName());
    private static final String SERVICE_NAME = "main-api";
    private static final String API_PATH = "/api/v1";

    private final CloseableHttpClient httpClient;

    @Inject
    private ConsulServiceDiscovery consulServiceDiscovery;

    public MainApiClient() {
        try {
            this.httpClient = HttpClients.custom()
                    .setSSLContext(createInsecureSSLContext())
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            LOGGER.info("MainApiClient initialized with Consul service discovery");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HTTP client", e);
        }
    }

    private String getMainApiUrl() {
        String baseUrl = consulServiceDiscovery.getServiceUrl(SERVICE_NAME);

        if (baseUrl == null) {
            // Fallback на прямой URL если Consul недоступен
            LOGGER.warning("Consul unavailable, using fallback URL");
            baseUrl = System.getProperty("main.api.url", "https://localhost:8443");
        }

        return baseUrl + API_PATH;
    }

    private javax.net.ssl.SSLContext createInsecureSSLContext() {
        try {
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    }
            }, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    public boolean bandExists(Integer bandId) {
        try {
            String mainApiUrl = getMainApiUrl();
            LOGGER.info("Checking if band exists, bandId: " + bandId + " at " + mainApiUrl);

            HttpGet httpGet = new HttpGet(mainApiUrl + "/bands/" + bandId);
            httpGet.setHeader("Accept", "application/xml");

            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());

            LOGGER.info("Band exists check response status: " + status);
            return status == 200;
        } catch (Exception e) {
            LOGGER.severe("Failed to check band existence: " + e.getMessage());
            throw new RuntimeException("Main API service unavailable: " + e.getMessage());
        }
    }

    public String getBandName(Integer bandId) {
        try {
            String mainApiUrl = getMainApiUrl();
            LOGGER.info("Fetching band name for bandId: " + bandId + " from " + mainApiUrl);

            HttpGet httpGet = new HttpGet(mainApiUrl + "/bands/" + bandId);
            httpGet.setHeader("Accept", "application/xml");

            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                String xml = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOGGER.info("Received XML: " + xml);

                int nameStart = xml.indexOf("<name>") + 6;
                int nameEnd = xml.indexOf("</name>");
                if (nameStart > 5 && nameEnd > nameStart) {
                    return xml.substring(nameStart, nameEnd);
                }
                LOGGER.warning("Failed to parse band name from XML");
                return null;
            }

            EntityUtils.consume(response.getEntity());
            LOGGER.warning("Failed to fetch band name, status: " + status);
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error fetching band name: " + e.getMessage());
            throw new RuntimeException("Main API service error: " + e.getMessage());
        }
    }

    public boolean updateParticipantsCount(Integer bandId, Integer newCount) {
        try {
            String mainApiUrl = getMainApiUrl();
            String patchXml = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<musicBand>" +
                            "<numberOfParticipants>%d</numberOfParticipants>" +
                            "</musicBand>",
                    newCount
            );
            LOGGER.info("Sending PATCH request for bandId: " + bandId + " to " + mainApiUrl + ", body: " + patchXml);

            HttpPatch httpPatch = new HttpPatch(mainApiUrl + "/bands/" + bandId);
            httpPatch.setHeader("Content-Type", "application/xml");
            httpPatch.setEntity(new StringEntity(patchXml, "UTF-8"));

            org.apache.http.HttpResponse response = httpClient.execute(httpPatch);
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());

            LOGGER.info("PATCH response status: " + status);
            return status == 200;
        } catch (Exception e) {
            LOGGER.severe("Error in PATCH request: " + e.getMessage());
            throw new RuntimeException("Main API service error: " + e.getMessage());
        }
    }
}