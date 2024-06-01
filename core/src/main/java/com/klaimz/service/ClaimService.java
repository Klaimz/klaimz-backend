package com.klaimz.service;


import com.klaimz.model.*;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.*;
import com.klaimz.util.Constants;
import com.klaimz.util.EntityValidators;
import com.klaimz.util.MongoUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.bson.conversions.Bson;

import java.util.List;

@Singleton
public class ClaimService {

    @Inject
    private ClaimRepository claimRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private EntityValidators entityValidators;

    @Inject
    private ClaimTemplateRepository claimTemplateRepository;

    @Inject
    private ClaimTypeRepository claimTypeRepository;

    @Inject
    private UserRepository userRepository;


    public Claim getClaimById(@NonNull String id) {

        var claim = claimRepository.findById(id);
        if (claim.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        return claim.get();
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> findByField(List<Filter> filters) {
        var matchFilter = MongoUtils.filterForClaims(filters);

        return claimRepository.findAll(matchFilter);
    }


    public Claim createClaim(@NonNull Claim claim, @NonNull String requesterId) {

        claim.setRequester(User.builder().id(requesterId).build());
        claim.setStatus(Constants.STATUS_NEW);
        entityValidators.validateClaim(claim);

        claim.setId(null); // ensure id is not set
        claim.setCreatedDate(null); // ensure created date is not set
        claim.setUpdateDate(null); // ensure update date is not set
        claim.setEvaluator(null); // ensure evaluator is not set
        claim.setClaimManager(null); // ensure claim manager is not set

        return updateClaim(claim, false);
    }


    public Claim updateClaim(Claim claim, boolean update) {
        entityValidators.validateClaim(claim);

        claim.getFields().forEach(field -> {
            if (field.getValue() == null) {
                field.setValue("");
            }
        });

        //  set product values form the product repository
        claim.getProducts().forEach(productDTO -> {
            var productObj = productRepository.findById(productDTO.getId());
            if (productObj.isEmpty()) {
                //  cant happen technically
                throw new IllegalArgumentException("Product not found");
            }

            productObj.ifPresent(product -> {
                productDTO.setMrp(product.getMrp());
                productDTO.setName(product.getName());
                productDTO.setUid(product.getUid());
                productDTO.setId(product.getId());
                productDTO.setGstPercentage(product.getGstPercentage());
            });
        });

        if (update) {
            claimRepository.update(claim);
        } else {
            claimRepository.save(claim);
        }

        return getClaimById(claim.getId());
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

    public ClaimType createClaimType(ClaimType claimType) {
        return claimTypeRepository.save(claimType);
    }

    public List<ClaimType> getAllClaimTypes() {
        return claimTypeRepository.findAll();
    }

}
