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
        // Читаем из system property или используем дефолт
        this.mainApiUrl = System.getProperty("main.api.url", "http://localhost:8080/api/v1");
        // Настройка клиента с Apache HttpClient и таймаутами
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ClientProperties.CONNECT_TIMEOUT, 5000); // Таймаут подключения 5 сек
        config.property(ClientProperties.READ_TIMEOUT, 10000);   // Таймаут чтения 10 сек
        this.client = ClientBuilder.newClient(config);
        LOGGER.info("MainApiClient initialized with URL: " + mainApiUrl);
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
        } catch (Exception e) {
            LOGGER.severe("Error checking band existence for bandId " + bandId + ": " + e.getMessage());
            return false;
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
                int nameStart = xml.indexOf("<name>") + 6;
                int nameEnd = xml.indexOf("</name>");
                if (nameStart > 2 && nameEnd > nameStart) {
                    return xml.substring(nameStart, nameEnd);
                }
                LOGGER.warning("Failed to parse band name XML for bandId: " + bandId);
                return null;
            }
            LOGGER.warning("Failed to fetch band name for bandId: " + bandId + ", status: " + response.getStatus());
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error fetching band name for bandId " + bandId + ": " + e.getMessage());
            return null;
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
                    .method("PATCH", Entity.xml(patchXml));

            LOGGER.info("PATCH response status: " + response.getStatus());
            if (response.getStatus() == 200) {
                return true;
            } else {
                String errorBody = response.readEntity(String.class);
                LOGGER.warning("PATCH request failed for bandId: " + bandId + ", status: " + response.getStatus() + ", body: " + errorBody);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Error in PATCH request for bandId " + bandId + ": " + e.getMessage());
            return false;
        } finally {
            if (response != null) {
                response.close();
                LOGGER.fine("Response closed for updateParticipantsCount, bandId: " + bandId);
            }
        }
    }
}