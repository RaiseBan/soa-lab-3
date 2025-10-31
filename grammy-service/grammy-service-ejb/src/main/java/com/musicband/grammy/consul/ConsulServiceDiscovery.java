package com.musicband.grammy.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@ApplicationScoped
public class ConsulServiceDiscovery {

    private static final Logger LOGGER = Logger.getLogger(ConsulServiceDiscovery.class.getName());
    
    private final ConsulClient consulClient;
    private final Random random = new Random();

    public ConsulServiceDiscovery() {
        String consulHost = System.getProperty("consul.host", "localhost");
        int consulPort = Integer.parseInt(System.getProperty("consul.port", "8500"));
        this.consulClient = new ConsulClient(consulHost, consulPort);
        LOGGER.info("ConsulServiceDiscovery initialized with Consul at " + consulHost + ":" + consulPort);
    }

    /**
     * Получить URL одного здорового инстанса сервиса (случайный выбор для балансировки)
     */
    public String getServiceUrl(String serviceName) {
        try {
            Response<List<HealthService>> healthyServices = 
                consulClient.getHealthServices(serviceName, true, QueryParams.DEFAULT);
            
            List<HealthService> services = healthyServices.getValue();
            
            if (services == null || services.isEmpty()) {
                LOGGER.warning("No healthy instances found for service: " + serviceName);
                return null;
            }

            // Случайный выбор инстанса для простой балансировки
            HealthService selectedService = services.get(random.nextInt(services.size()));
            
            String address = selectedService.getService().getAddress();
            int port = selectedService.getService().getPort();
            
            String url = "https://" + address + ":" + port;
            LOGGER.info("Selected service instance: " + url + " for " + serviceName);
            
            return url;
            
        } catch (Exception e) {
            LOGGER.severe("Failed to discover service " + serviceName + " from Consul: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получить все здоровые инстансы сервиса
     */
    public List<HealthService> getHealthyInstances(String serviceName) {
        try {
            Response<List<HealthService>> response = 
                consulClient.getHealthServices(serviceName, true, QueryParams.DEFAULT);
            return response.getValue();
        } catch (Exception e) {
            LOGGER.severe("Failed to get healthy instances for " + serviceName + ": " + e.getMessage());
            return List.of();
        }
    }
}