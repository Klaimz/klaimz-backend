package com.klaimz.service;


import com.klaimz.model.Claim;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.ClaimRepository;
import com.klaimz.repo.ClaimTemplateRepository;
import com.klaimz.util.EntityValidators;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class ClaimService {

    @Inject
    private ClaimRepository claimRepository;


    @Inject
    private EntityValidators entityValidators;

    @Inject
    private ClaimTemplateRepository claimTemplateRepository;

    // get claim by id
    public Claim getClaimById(String id) {
        var claim = claimRepository.findById(id);
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


    public Claim createClaim(Claim claim) {
        entityValidators.validateClaim(claim);

        claim.setId(null); // ensure id is not set
        claim.setCreatedDate(null); // ensure created date is not set
        claim.setUpdateDate(null); // ensure update date is not set

        return claimRepository.save(claim);
    }


    public Claim updateClaim(Claim claim) {
        entityValidators.validateClaim(claim);

        return claimRepository.update(claim);
    }

    public Claim addComment(String claimId, String comment, String user) {
        var claim = claimRepository.findById(claimId);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        var claimObj = claim.get();

        claimObj.addComment(comment, user);

        return claimRepository.update(claimObj);
    }

    public Object updateStatus(String id, String status, String user) {
        var claim = claimRepository.findById(id);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        var claimObj = claim.get();

        claimObj.updateStatus(status, user);

        return claimRepository.update(claimObj);
    }
}
