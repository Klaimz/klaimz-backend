package com.klaimz.service;

import com.klaimz.model.FormField;
import com.klaimz.repo.FormFieldRepository;
import com.klaimz.util.Constants;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;


@Singleton
@Requires(notEnv = "test")
public class DataLoader implements ApplicationEventListener<StartupEvent> {

    @Inject
    private FormFieldRepository formFieldRepository;

    @Override
    public void onApplicationEvent(StartupEvent event) {
        System.out.println("DataLoader is up and running!");
//        var count = formFieldRepository.count();
//        if (count == 0) {
//            System.out.println("No form fields found, creating some...");
//            formFieldRepository.saveAll(Constants.FORM_FIELDS);
//        }
    }

    @Override
    public boolean supports(StartupEvent event) {
        return ApplicationEventListener.super.supports(event);
    }
}
