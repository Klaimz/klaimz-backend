package com.klaimz.api;


import com.klaimz.model.Claim;
import com.klaimz.model.api.GenericDto;
import com.klaimz.model.api.Filter;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.ClaimService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.security.Principal;

import static com.klaimz.util.HttpUtils.*;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/claim")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ClaimController {
    @Inject
    private ClaimService claimService;

    @Inject
    private AwsS3Operations objectStorage;


    @Get("/{id}")
    public HttpResponse<MessageBean> getClaimById(@NonNull String id) {
        var claim = claimService.getClaimById(id);
        return success(claim, "Claim found");
    }

    @Post("/{id}/comment")
    public HttpResponse<MessageBean> addComment(@NonNull String id, @Body GenericDto comment, @NonNull Principal principal) {
        var userId = principal.getName();
        var claim = claimService.addComment(id, comment.getBody(), userId);
        return success(claim, "Comment added");
    }

    @Post("/{id}/status")
    public HttpResponse<MessageBean> updateStatus(@NonNull String id, @Body GenericDto status, @NonNull Principal principal) {
        var userId = principal.getName();
        var claim = claimService.updateStatus(id, status.getBody(), userId);
        return success(claim, "Claim status updated");
    }

    @Post(consumes = MediaType.MULTIPART_FORM_DATA, value = "/{id}/{fieldKey}/upload")
    public HttpResponse<MessageBean> upload(@PathVariable String fieldKey, @PathVariable String id, CompletedFileUpload file) {
        var claim = claimService.getClaimById(id);

        var field = claim.getField(fieldKey);

        var fileExtension = file.getFilename().substring(file.getFilename().lastIndexOf("."));

        UploadRequest objectStorageUpload = UploadRequest.fromCompletedFileUpload(file);
        var filePath = "claim/" + id + "/" + fieldKey + "/" + System.currentTimeMillis() + fileExtension;
        objectStorage.upload(objectStorageUpload, builder -> {
            builder.key(filePath);
        });

        claim.updateField(fieldKey, filePath);
        claimService.updateClaim(claim);

        return success(filePath, "File uploaded successfully");
    }

    //     download file
    @Get("/{id}/{fieldKey}/download")
    public HttpResponse download(@PathVariable String fieldKey, @PathVariable String id) {
        var claim = claimService.getClaimById(id);

        var field = claim.getField(fieldKey);

        var filePath = field.getValue();
        var file = objectStorage.retrieve(filePath);
        if (file.isEmpty()) {
            return notFound("File not found");
        }

        return HttpResponse.ok(file.get().getInputStream());
    }

    @Post
    public HttpResponse<MessageBean> createClaim(@Body Claim claim) {
        var newClaim = claimService.createClaim(claim);
        return success(newClaim, "Claim created successfully");
    }

    @Patch("/{id}")
    public HttpResponse<MessageBean> updateClaim(String id, @Body Claim claim) {

        if (!claim.getId().equals(id)) {
            return badRequest("Claim id mismatch");
        }

        var updatedClaim = claimService.updateClaim(claim);
        return success(updatedClaim, "Claim updated successfully");
    }


    @Get("/all")
    public HttpResponse<MessageBean> getAllClaims() {
        var claims = claimService.getAllClaims();
        if (claims.isEmpty()) {
            return notFound("No claims found");
        }
        return success(claims, "Claims found");
    }

    @Post("/search")
    public HttpResponse<MessageBean> search(@Body Filter filter) {
        var result = claimService.findByField(filter);
        if (result.isEmpty()) {
            return notFound("No claim found");
        }

        return success(result, "Claim search result");
    }
}
