package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.Product;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.Function;

import static com.klaimz.util.StringUtils.*;

@Singleton
public class EntityValidators {

    public List<Function<Product, String>> PRODUCT_VALIDATORS = List.of(
            emptyCheck(Product::getUid, "Product must have a UID"),
            emptyCheck(Product::getName, "Product must have a name"),
            emptyCheck(Product::getBatchNumber, "Product must have a batch number"),
            product -> {
                if (product.getGstPercentage() < 0 || product.getGstPercentage() > 100) {
                    return "Product GST percentage must be between 0 and 100";
                }
                return VERIFIED;
            },
            product -> {
                if (product.getQuantity() < 0) {
                    return "Product quantity must be greater than 0";
                }
                return VERIFIED;
            }
    );


    private List<Function<Claim, String>> CLAIM_VALIDATORS = List.of(
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
//                TODO: check if claim template exists
//                claimTemplateRepository.findAll();
//                var template = claimTemplateRepository.findById(claim.getClaimTemplateId());
//                if (template.isEmpty()) {
//                    return "Claim template not found";
//                }
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
