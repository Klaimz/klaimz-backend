package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.repo.ProductRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.klaimz.util.Constants.STATUS_CM_ASSIGNED;
import static com.klaimz.util.Constants.STATUS_EVALUATOR_ASSIGNED;
import static com.klaimz.util.StringUtils.*;

@Singleton
public class EntityValidators {
    @Inject
    private ProductRepository productRepository;

    private final List<Function<Claim, String>> CLAIM_VALIDATORS = List.of(
            claim -> {
                //  check if all products exist
                var isAllProductsValid = claim.getProducts().stream().map(Claim.ProductDTO::getId)
                        .map(productRepository::findById)
                        .noneMatch(Optional::isEmpty);
                if (!isAllProductsValid) {
                    return "Claim must have valid products,check the product ids";
                }

                return VERIFIED;
            },
            claim -> {
                if (!Constants.STATUS_LIST.contains(claim.getStatus())) {
                    return "Claim status is invalid";
                }
                return VERIFIED;
            }
    );



    public void validateClaim(Claim claim) {
        for (var validator : CLAIM_VALIDATORS) {
            var result = validator.apply(claim);
            if (!result.equals(VERIFIED)) {
                throw new IllegalArgumentException(result);
            }
        }
    }

}
