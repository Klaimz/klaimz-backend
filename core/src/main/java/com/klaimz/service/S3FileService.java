package com.klaimz.service;


import com.klaimz.model.Claim;
import com.klaimz.model.api.PresignedUrlDto;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Singleton
public class S3FileService {


    @Value("${S3_BUCKET}")
    private String bucket;

    @Inject
    private ClaimService claimService;

    @Inject
    S3Client s3Client;

    @Inject
    S3Presigner s3Presigner;


    public PresignedUrlDto generatePresignedPutUrl(String id, String fieldKey, String fileName) {
        var claim = claimService.getClaimById(id);
        var field = claim.getField(fieldKey);
        var path = getPath(fileName, claim, field);

        return PresignedUrlDto.builder()
                .fileName(fileName)
                .url(createPresignedPutUrl(bucket, path))
                .build();
    }

    private static String getPath(String fileName, Claim claim, Claim.FormFieldValue field) {
        return "claims/" + claim.getId() + "/" + field.getKey() + "/" + fileName;
    }

    public PresignedUrlDto generatePresignedGetUrl(String id, String fieldKey,String fileName) {
        var claim = claimService.getClaimById(id);
        var field = claim.getField(fieldKey);
        var path = getPath(fileName, claim, field);

        return PresignedUrlDto.builder()
                .url(createPresignedGetUrl(bucket, path))
                .build();
    }

    private String createPresignedPutUrl(String bucketName, String keyName) {

        var objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        var presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toExternalForm();
    }

    private String createPresignedGetUrl(String bucketName, String keyName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(20))  // The URL will expire in 10 minutes.
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toExternalForm();
    }
}