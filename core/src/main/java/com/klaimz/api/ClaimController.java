package com.klaimz.api;


import com.klaimz.model.Claim;
import com.klaimz.model.api.Filter;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.ClaimService;
import com.klaimz.util.HttpUtils;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import static com.klaimz.util.HttpUtils.*;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/claim")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ClaimController {
    @Inject
    private ClaimService claimService;

    @Get("/{id}")
    public HttpResponse<MessageBean> getClaimById(@NonNull String id) {

        var claim =  claimService.getClaimById(id);
        if (claim.isEmpty()) {
            return notFound("Claim not found");
        }
        return success(claim, "Claim found");
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
        var claims =  claimService.getAllClaims();
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
