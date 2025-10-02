package com.musicband.grammy.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration для Grammy Service
 * Определяет базовый путь для REST API: /grammy
 */
@ApplicationPath("/")
public class RestApplication extends Application {
    // JAX-RS автоматически найдет все классы с аннотацией @Path
}