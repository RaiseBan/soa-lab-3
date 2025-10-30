package com.musicband.grammy.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.util.logging.Logger;

@ApplicationScoped
public class MainApiClient {

    private static final Logger LOGGER = Logger.getLogger(MainApiClient.class.getName());
    private final String mainApiUrl;
    private final Client client;

    public MainApiClient() {
        this.mainApiUrl = System.getProperty("main.api.url", "https://localhost:8443/api/v1");

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ClientProperties.CONNECT_TIMEOUT, 5000);
        config.property(ClientProperties.READ_TIMEOUT, 10000);

        // ДОБАВЬТЕ ЭТИ СТРОКИ для игнорирования SSL в dev окружении:
        config.property(ClientProperties.FOLLOW_REDIRECTS, false);

        this.client = ClientBuilder.newBuilder()
                .withConfig(config)
                .hostnameVerifier((hostname, session) -> true) // Игнорировать hostname verification
                .sslContext(createInsecureSSLContext()) // Игнорировать SSL сертификат
                .build();

        LOGGER.info("MainApiClient initialized with URL: " + mainApiUrl);
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД:
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
        Response response = null;
        try {
            LOGGER.info("Checking if band exists, bandId: " + bandId);
            response = client.target(mainApiUrl)
                    .path("bands")
                    .path(String.valueOf(bandId))
                    .request(MediaType.APPLICATION_XML)
                    .get();
            LOGGER.info("Band exists check response status: " + response.getStatus());
            return response.getStatus() == 200;
        } catch (jakarta.ws.rs.ProcessingException e) {
            LOGGER.severe("Failed to connect to Main API at " + mainApiUrl + ": " + e.getMessage());
            throw new RuntimeException("Main API service unavailable: Unable to connect to " + mainApiUrl);
        } catch (Exception e) {
            LOGGER.severe("Error checking band existence for bandId " + bandId + ": " + e.getMessage());
            throw new RuntimeException("Main API service error: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
                LOGGER.fine("Response closed for bandExists, bandId: " + bandId);
            }
        }
    }

    public String getBandName(Integer bandId) {
        Response response = null;
        try {
            LOGGER.info("Fetching band name for bandId: " + bandId);
            response = client.target(mainApiUrl)
                    .path("bands")
                    .path(String.valueOf(bandId))
                    .request(MediaType.APPLICATION_XML)
                    .get();
            LOGGER.info("Get band name response status: " + response.getStatus());
            if (response.getStatus() == 200) {
                String xml = response.readEntity(String.class);
                LOGGER.info("Received XML: " + xml);
                int nameStart = xml.indexOf("<n>") + 3;
                int nameEnd = xml.indexOf("</n>");
                if (nameStart > 2 && nameEnd > nameStart) {
                    return xml.substring(nameStart, nameEnd);
                }
                LOGGER.warning("Failed to parse band name XML for bandId: " + bandId);
                return null;
            }
            LOGGER.warning("Failed to fetch band name for bandId: " + bandId + ", status: " + response.getStatus());
            return null;
        } catch (jakarta.ws.rs.ProcessingException e) {
            LOGGER.severe("Failed to connect to Main API at " + mainApiUrl + ": " + e.getMessage());
            throw new RuntimeException("Main API service unavailable: Unable to connect to " + mainApiUrl);
        } catch (Exception e) {
            LOGGER.severe("Error fetching band name for bandId " + bandId + ": " + e.getMessage());
            throw new RuntimeException("Main API service error: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
                LOGGER.fine("Response closed for getBandName, bandId: " + bandId);
            }
        }
    }

    public boolean updateParticipantsCount(Integer bandId, Integer newCount) {
        Response response = null;
        try {
            String patchXml = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<musicBand>" +
                            "<numberOfParticipants>%d</numberOfParticipants>" +
                            "</musicBand>",
                    newCount
            );
            LOGGER.info("Sending PATCH request for bandId: " + bandId + ", body: " + patchXml);
            response = client.target(mainApiUrl)
                    .path("bands")
                    .path(String.valueOf(bandId))
                    .request(MediaType.APPLICATION_XML)
                    .method("PATCH", Entity.entity(patchXml, MediaType.TEXT_XML));  // TEXT_XML вместо APPLICATION_XML

            LOGGER.info("PATCH response status: " + response.getStatus());
            if (response.getStatus() == 200) {
                return true;
            } else {
                String errorBody = response.readEntity(String.class);
                LOGGER.warning("PATCH request failed for bandId: " + bandId + ", status: " + response.getStatus() + ", body: " + errorBody);
                return false;
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            LOGGER.severe("Failed to connect to Main API at " + mainApiUrl + ": " + e.getMessage());
            throw new RuntimeException("Main API service unavailable: Unable to connect to " + mainApiUrl);
        } catch (Exception e) {
            LOGGER.severe("Error in PATCH request for bandId " + bandId + ": " + e.getMessage());
            throw new RuntimeException("Main API service error: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
                LOGGER.fine("Response closed for updateParticipantsCount, bandId: " + bandId);
            }
        }
    }
}
