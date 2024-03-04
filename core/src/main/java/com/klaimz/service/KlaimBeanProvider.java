package com.klaimz.service;


import io.micronaut.context.annotation.Factory;
import io.micronaut.http.client.StreamingHttpClient;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Factory
public class KlaimBeanProvider {
    @Singleton
    public S3Presigner s3PresignerService() {
        return S3Presigner.create();
    }


}

