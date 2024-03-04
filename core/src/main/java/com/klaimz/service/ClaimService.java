package com.klaimz.service;


import com.klaimz.model.Claim;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.ClaimRepository;
import com.klaimz.repo.ClaimTemplateRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.Function;

import static com.klaimz.util.StringUtils.*;

@Singleton
public class ClaimService {

    @Inject
    private ClaimRepository claimRepository;

    @Inject
    private ClaimTemplateRepository claimTemplateRepository;

    // get claim by id
    public Claim getClaimById(String id) {
        var claim =  claimRepository.findById(id);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        return claim.get();
    }

    // get all claims
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> findByField(Filter filter) {
        return claimRepository.findAll(ClaimRepository.Specification.findByField(filter.getField(), filter.getValue()));
    }

    List<Function<Claim, String>> validators = List.of(
            emptyCheck(Claim::getDescription, "Claim must have a description"),
            emptyCheck(Claim::getAmount, "Claim amount must have a value"),
            emptyCheck(Claim::getStatus, "Claim must have a status"),
            // claim amount must be greater than 0 and valid currency
            claim -> {
                if (!isCurrency(claim.getAmount())) {
                    return "Claim amount must be a valid currency";
                }
                return VERIFIED;
            },
//            assumes must have form fields, later when we have form fields in the claim
            claim -> {
                if (claim.getFields() == null || claim.getFields().isEmpty()) {
                    return "Claim must have form fields";
                }
                var keys = claim.getFields().stream().map(Claim.FormFieldValue::getKey).toList();
                if (keys.size() != keys.stream().distinct().count()) {
                    return "Claim fields must have unique keys, ie no duplicate keys";
                }

                // verify for each of the form field  the type and key is not empty
                var keyFun = emptyCheck(Claim.FormFieldValue::getKey, "Claim field key must not be null or empty");
                var typeFun = emptyCheck(Claim.FormFieldValue::getType, "Claim field type must not be null or empty");
                return claim.getFields().stream().map(field -> {
                    var keyResult = keyFun.apply(field);
                    if (!keyResult.equals(VERIFIED)) {
                        return keyResult;
                    }
                    return typeFun.apply(field);
                }).filter(result -> !result.equals(VERIFIED)).findFirst().orElse(VERIFIED);
            },
//            claim must have a requester
            emptyCheck(Claim::getRequesterUserId, "Claim must have a requester"),
//            must have a claim template id
            claim -> {
                if (claim.getClaimTemplateId() == null || claim.getClaimTemplateId().isBlank()) {
                    return "Claim must not have a empty template id";
                }
//                claimTemplateRepository.findAll();
//                var template = claimTemplateRepository.findById(claim.getClaimTemplateId());
//                if (template.isEmpty()) {
//                    return "Claim template not found";
//                }
                return VERIFIED;
            }
    );

    private void validateClaim(Claim claim) {
        for (var validator : validators) {
            var result = validator.apply(claim);
            if (!result.equals(VERIFIED)) {
                throw new IllegalArgumentException(result);
            }
        }
    }

    public Claim createClaim(Claim claim) {
        validateClaim(claim);

        claim.setId(null); // ensure id is not set
        claim.setCreatedDate(null); // ensure created date is not set
        claim.setUpdateDate(null); // ensure update date is not set

        return claimRepository.save(claim);
    }


    public Claim updateClaim(Claim claim) {
        validateClaim(claim);

        return claimRepository.update(claim);
    }

    public Claim addComment(String claimId, String comment,String user) {
        var claim = claimRepository.findById(claimId);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        var claimObj = claim.get();

        claimObj.addComment(comment, user);

        return claimRepository.update(claimObj);
    }

    public Object updateStatus(String id, String status,String user) {
        var claim = claimRepository.findById(id);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        var claimObj = claim.get();

        claimObj.updateStatus(status, user);

        return claimRepository.update(claimObj);
    }
}
