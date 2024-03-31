package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.Product;
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

    public final List<Function<Product, String>> PRODUCT_VALIDATORS = List.of(
            emptyCheck(Product::getUid, "Product must have a UID"),
            emptyCheck(Product::getName, "Product must have a name"),
            product -> {
                if (product.getGstPercentage() < 0 || product.getGstPercentage() > 100) {
                    return "Product GST percentage must be between 0 and 100";
                }
                return VERIFIED;
            },
            product -> {
                if (product.getMrp() > 0) {
                    return "Product mrp must be greater than 0";
                }
                return VERIFIED;
            }
    );


    private final List<Function<Claim, String>> CLAIM_VALIDATORS = List.of(
            emptyCheck(Claim::getAmount, "Claim amount must have a value"),
            emptyCheck(Claim::getStatus, "Claim must have a status"),
            emptyCheck(Claim::getRequesterUserId, "Claim must have a requester"),
            emptyCheck(Claim::getClaimManagerUserId, "Claim must have an evaluator",claim -> claim.getStatus().equals(STATUS_CM_ASSIGNED)),
            emptyCheck(Claim::getEvaluatorUserId, "Claim must have a claim manager",claim -> claim.getStatus().equals(STATUS_EVALUATOR_ASSIGNED)),
            // claim amount must be greater than 0 and valid currency
            claim -> {
                if (!isCurrency(claim.getAmount())) {
                    return "Claim amount must be a valid currency";
                }
                return VERIFIED;
            },
            // assumes must have form fields, later when we have form fields in the claim
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
                // TODO: check if claim template exists
                // claimTemplateRepository.findAll();
                // var template = claimTemplateRepository.findById(claim.getClaimTemplateId());
                // if (template.isEmpty()) {
                //     return "Claim template not found";
                // }
                return VERIFIED;
            },
            claim -> {
                if (claim.getProducts() == null || claim.getProducts().isEmpty()) {
                    return "Claim must have products";
                }

                //  check if all products exist
                var isAllProductsValid = claim.getProducts().stream().map(Claim.ProductDTO::getId)
                        .map(productRepository::findById)
                        .noneMatch(Optional::isEmpty);
                if (!isAllProductsValid) {
                    return "Claim must have valid products,check the product ids";
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

    public void validateProduct(Product product) {
        for (var validator : PRODUCT_VALIDATORS) {
            var result = validator.apply(product);
            if (!result.equals(VERIFIED)) {
                throw new IllegalArgumentException(result);
            }
        }
    }

}
