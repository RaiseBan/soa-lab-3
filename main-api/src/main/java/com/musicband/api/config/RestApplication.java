package com.musicband.api.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration
 * Определяет базовый путь для REST API: /api/v1
 */
@ApplicationPath("/api/v1")
public class RestApplication extends Application {
    // JAX-RS автоматически найдет все классы с аннотацией @Path
}