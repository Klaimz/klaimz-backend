package com.klaimz.api;


import com.klaimz.model.Claim;
import com.klaimz.model.api.Filter;
import com.klaimz.model.api.GenericDto;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.ClaimService;
import com.klaimz.service.S3FileService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
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
    private S3FileService s3FileService;


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

    @Post(  "/{id}/{fieldKey}/upload")
    public HttpResponse<MessageBean> upload(@PathVariable String fieldKey, @PathVariable String id,@QueryValue("file") String fileName) {
        var claim = claimService.getClaimById(id);
        var field = claim.getField(fieldKey);

        if (field.getValue().contains(fileName)) {
            return badRequest("File already uploaded");
        }

       // check if the file name doesn't contain any special characters
        if (!fileName.matches("^[a-zA-Z0-9_.-]*$")) {
            return badRequest("Invalid file name");
        }

        // generate pre-signed URL for the file upload
        var presignedUrlDto  = s3FileService.generatePresignedPutUrl(id, fieldKey, fileName);

        // update the field value with the new file name
        var newFieldValue = field.getValue() + ";" + fileName;

        claim.updateField(fieldKey, newFieldValue);
        claimService.updateClaim(claim);

        return success(presignedUrlDto, "Pre-signed URL generated successfully");
    }

    @Get("/{id}/{fieldKey}/download")
    public HttpResponse download(@PathVariable String fieldKey, @PathVariable String id,@QueryValue("file") String fileName) throws URISyntaxException {
        var presignedUrlDto = s3FileService.generatePresignedGetUrl(id, fieldKey,fileName);
        var url = presignedUrlDto.getUrl();

        return HttpResponse.redirect(new URI(url));
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
