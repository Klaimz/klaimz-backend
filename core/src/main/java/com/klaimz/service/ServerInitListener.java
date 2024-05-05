package com.klaimz.service;

import com.klaimz.repo.FormFieldRepository;
import com.klaimz.util.Constants;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.security.NoSuchAlgorithmException;


@Singleton
@Requires(notEnv = "test")
public class ServerInitListener implements ApplicationEventListener<StartupEvent> {

    @Inject
    private SystemPropertiesService systemPropertiesService;

    @Override
    public void onApplicationEvent(StartupEvent event) {
//            don't do anything here, single it can slow down a startup in Lambda
    }

    @Override
    public boolean supports(StartupEvent event) {
        return ApplicationEventListener.super.supports(event);
    }
}
