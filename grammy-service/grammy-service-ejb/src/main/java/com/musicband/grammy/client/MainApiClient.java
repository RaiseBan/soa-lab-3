package com.musicband.grammy.client;

import jakarta.enterprise.context.ApplicationScoped;
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
    private final String mainApiUrl;
    private final CloseableHttpClient httpClient;

    public MainApiClient() {
        this.mainApiUrl = System.getProperty("main.api.url", "https://localhost:8443/api/v1");

        try {
            this.httpClient = HttpClients.custom()
                    .setSSLContext(createInsecureSSLContext())
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            LOGGER.info("MainApiClient initialized with URL: " + mainApiUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HTTP client", e);
        }
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
            LOGGER.info("Checking if band exists, bandId: " + bandId);
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
            LOGGER.info("Fetching band name for bandId: " + bandId);
            HttpGet httpGet = new HttpGet(mainApiUrl + "/bands/" + bandId);
            httpGet.setHeader("Accept", "application/xml");

            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                String xml = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOGGER.info("Received XML: " + xml);

                int nameStart = xml.indexOf("<n>") + 6;
                int nameEnd = xml.indexOf("</n>");
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
            String patchXml = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<musicBand>" +
                            "<numberOfParticipants>%d</numberOfParticipants>" +
                            "</musicBand>",
                    newCount
            );
            LOGGER.info("Sending PATCH request for bandId: " + bandId + ", body: " + patchXml);

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