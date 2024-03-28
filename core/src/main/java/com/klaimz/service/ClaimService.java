package com.klaimz.service;


import com.klaimz.model.Claim;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.ClaimRepository;
import com.klaimz.repo.ClaimTemplateRepository;
import com.klaimz.repo.UserRepository;
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

    @Inject
    private UserRepository userRepository;

    public Claim getClaimById(String id) {
        var claim = claimRepository.findById(id);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        return claim.get();
    }

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
        claim.setEvaluatorUserId(null); // ensure evaluator is not set
        claim.setClaimManagerUserId(null); // ensure claim manager is not set


        var requester = userRepository.findById(claim.getRequesterUserId());

        if (requester.isEmpty()) {
            throw new IllegalArgumentException("Requester not found");
        }

        claim.setRequester(requester.get());

        return claimRepository.save(claim);
    }


    public Claim updateClaim(Claim claim) {
        entityValidators.validateClaim(claim);

        if (claim.getEvaluatorUserId() != null) {
            var evaluator = userRepository.findById(claim.getEvaluatorUserId());
            if (evaluator.isEmpty()) {
                throw new IllegalArgumentException("Evaluator not found");
            }
            claim.setEvaluator(evaluator.get());
        }

        if (claim.getClaimManagerUserId() != null) {
            var claimManager = userRepository.findById(claim.getClaimManagerUserId());
            if (claimManager.isEmpty()) {
                throw new IllegalArgumentException("Claim manager not found");
            }
            claim.setClaimManager(claimManager.get());
        }

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
