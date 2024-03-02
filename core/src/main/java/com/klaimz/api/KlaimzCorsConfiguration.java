package com.klaimz.api;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.cors.CorsOriginConfiguration;

import java.util.List;
import java.util.Map;

@ConfigurationProperties("micronaut.server.cors")
public class KlaimzCorsConfiguration extends HttpServerConfiguration.CorsConfiguration {
    public KlaimzCorsConfiguration() {
        super();
        this.setEnabled(true);
        this.setLocalhostPassThrough(true);
    }
    @Override
    public Map<String, CorsOriginConfiguration> getConfigurations() {
        var configurations = super.getConfigurations();
        if (configurations.isEmpty()) {
            var origins = List.of("*");
            var config = new CorsOriginConfiguration();
            config.setAllowedOrigins(origins);
            config.setAllowedMethods(List.of(HttpMethod.GET, HttpMethod.POST,
                    HttpMethod.PUT,HttpMethod.PATCH,
                    HttpMethod.TRACE, HttpMethod.DELETE,
                    HttpMethod.OPTIONS));
            config.setExposedHeaders(List.of("Content-Type","Authorization","*"));
            config.setAllowedHeaders(List.of("Content-Type","Authorization","X-Requested-With","Content-Length","Accept","Origin"));
            configurations.put("web",config);
        }

        return configurations;
    }
}
