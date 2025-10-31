package com.musicband.api.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.net.InetAddress;
import java.util.Collections;
import java.util.logging.Logger;

@Singleton
@Startup
public class ConsulServiceRegistry {

    private static final Logger LOGGER = Logger.getLogger(ConsulServiceRegistry.class.getName());

    private static final String SERVICE_NAME = "main-api";
    private String serviceId;
    private static final String consulHost = "localhost";
    private int consulPort;
    private static final String serviceHost = "localhost";
    private int servicePort;

    private ConsulClient consulClient;

    @PostConstruct
    public void registerService() {
        try {
            LOGGER.info("Registering service with Consul...");

            
            consulPort = Integer.parseInt(System.getProperty("consul.port", "8500"));

            servicePort = Integer.parseInt(System.getProperty("service.port", "8228"));

            
            serviceId = System.getProperty("service.id",
                    SERVICE_NAME + "-" + serviceHost + "-" + servicePort);

            LOGGER.info(String.format("Service parameters: host=%s, port=%d, id=%s",
                    serviceHost, servicePort, serviceId));

            consulClient = new ConsulClient(consulHost, consulPort);

            NewService newService = new NewService();
            newService.setId(serviceId);
            newService.setName(SERVICE_NAME);
            newService.setAddress(serviceHost);
            newService.setPort(servicePort);
            newService.setTags(Collections.singletonList("music-band-api"));

            
            NewService.Check check = new NewService.Check();
            check.setHttp("https://" + serviceHost + ":" + servicePort + "/api/v1/bands");
            check.setInterval("10s");
            check.setTimeout("5s");
            check.setTlsSkipVerify(true);  
            newService.setCheck(check);

            consulClient.agentServiceRegister(newService);

            LOGGER.info("Service registered successfully with Consul: " + serviceId);

        } catch (Exception e) {
            LOGGER.severe("Failed to register service with Consul: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void deregisterService() {
        try {
            if (consulClient != null) {
                LOGGER.info("Deregistering service from Consul: " + serviceId);
                consulClient.agentServiceDeregister(serviceId);
                LOGGER.info("Service deregistered successfully from Consul");
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to deregister service from Consul: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            LOGGER.warning("Failed to get local hostname, using 'localhost'");
            return "localhost";
        }
    }
}