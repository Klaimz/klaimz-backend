package com.klaimz.api;


import com.klaimz.model.Claim;
import com.klaimz.model.ClaimType;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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
    public HttpResponse<MessageBean> getClaimById(@NotBlank String id) {
        var claim = claimService.getClaimById(id);
        return success(claim, "Claim found");
    }

    @Post("/{id}/comment")
    public HttpResponse<MessageBean> addComment(@NotBlank String id, @Valid @Body GenericDto comment, @NonNull Principal principal) {
        var userId = principal.getName();
        var claim = claimService.addComment(id, comment.getBody(), userId);
        return success(claim, "Comment added");
    }

    @Post("/{id}/status")
    public HttpResponse<MessageBean> updateStatus(@NonNull String id, @Valid @Body GenericDto status, @NonNull Principal principal) {
        var userId = principal.getName();


        var claim = claimService.updateStatus(id, status.getBody(), userId);
        return success(claim, "Claim status updated");
    }

    @Post("/{id}/{fieldKey}/upload")
    public HttpResponse<MessageBean> upload(@NotBlank @PathVariable String fieldKey, @NotBlank @PathVariable String id, @NotBlank @QueryValue("file") String fileName) {
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
        var presignedUrlDto = s3FileService.generatePresignedPutUrl(id, fieldKey, fileName);

        // update the field value with the new file name
        var newFieldValue = field.getValue().isEmpty() ? fileName : field.getValue() + ";" + fileName;

        claim.updateField(fieldKey, newFieldValue);
        claimService.updateClaim(claim, true);

        return success(presignedUrlDto, "Pre-signed URL generated successfully");
    }

    @Get("/{id}/{fieldKey}/download")
    public HttpResponse download(@NotBlank @PathVariable String fieldKey, @NotBlank @PathVariable String id, @NotBlank @QueryValue("file") String fileName) throws URISyntaxException {
        var presignedUrlDto = s3FileService.generatePresignedGetUrl(id, fieldKey, fileName);
        var url = presignedUrlDto.getUrl();

        return HttpResponse.redirect(new URI(url));
    }

    @Post
    public HttpResponse<MessageBean> createClaim(@Valid @Body Claim claim, @NonNull Principal principal) {
        var newClaim = claimService.createClaim(claim, principal.getName());
        return success(newClaim, "Claim created successfully");
    }

    @Patch("/{id}")
    public HttpResponse<MessageBean> updateClaim(String id, @Valid @Body Claim claim) {

        if (!claim.getId().equals(id)) {
            return badRequest("Claim id mismatch");
        }

        var updatedClaim = claimService.updateClaim(claim, true);
        return success(updatedClaim, "Claim updated successfully");
    }


    @Get("/all")
    public HttpResponse<MessageBean> getAllClaims() {
        var claims = claimService.getAllClaims();

        return success(claims, "Claims found");
    }

    @Post("/search")
    public HttpResponse<MessageBean> search(@Valid @Body List<Filter> filters) {
        var result = claimService.findByField(filters);
        return success(result, "Claim search result");
    }

    @Get("/types")
    public HttpResponse<MessageBean> getClaimTypes() {
        var claimTypes = claimService.getAllClaimTypes();

        return success(claimTypes, "Claim types found");
    }

    @Post("/types")
    public HttpResponse<MessageBean> createClaimType(@Valid @Body ClaimType claimType) {
        var newClaimType = claimService.createClaimType(claimType);
        return success(newClaimType, "Claim type created successfully");
    }
}
