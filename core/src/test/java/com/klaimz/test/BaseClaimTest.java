package com.klaimz.test;

import com.klaimz.model.Claim;
import com.klaimz.model.Product;
import com.klaimz.model.User;
import com.klaimz.repo.ClaimRepository;
import com.klaimz.repo.ClaimTypeRepository;
import com.klaimz.repo.ProductRepository;
import com.klaimz.repo.UserRepository;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.klaimz.util.Constants.*;

public abstract class BaseClaimTest {
    public final int MAX_CLAIMS_SZIE = 1000;
    public final int MAX_USERS_SIZE = 300;
    public final int MAX_PRODUCTS_SIZE = 200;

    final Random random = new Random();

    @Data
    @Builder
    public static class TestDataContainer {
        private List<Claim> claims;
        private List<Product> products;
        private List<User> users;
        private long id;
    }

    abstract ClaimRepository getClaimRepository();

    abstract ClaimTypeRepository getClaimTypeRepository();

    abstract UserRepository getUserRepository();

    abstract ProductRepository getProductRepository();


    ClaimServiceTest.TestDataContainer setUpData(int maxClaims) {

        var userData =
                IntStream.range(0, MAX_USERS_SIZE)
                        .mapToObj(it -> getUserRepository().save(generateRandomUser()))
                        .toList();

        var productData =
                IntStream.range(0, MAX_PRODUCTS_SIZE)
                        .mapToObj(it -> getProductRepository().save(generateRandomProduct()))
                        .toList();

        var claimData = IntStream.range(0, maxClaims)
                .mapToObj(it -> getClaimRepository().save(generateRandomClaim(userData, productData)))
                .toList();

        return ClaimServiceTest.TestDataContainer.builder()
                .claims(claimData)
                .products(productData)
                .users(userData)
                .id(random.nextLong())
                .build();
    }


    User generateRandomUser() {
        Random random = new Random();
        return User.builder()
                .displayName("Random-User-" + random.nextInt(1000))
                .email("randomUser" + random.nextInt(1000) + "@klaimz.com")
                .gstNumber("123-456-" + String.format("%04d", random.nextInt(1000)))
                .build();
    }

    Product generateRandomProduct() {
        Random random = new Random();
        return Product.builder()
                .name("Random Product " + random.nextInt(1000))
                .uid("RP" + random.nextInt(1000))
                .gstPercentage((random.nextDouble() * 28)+5)
                .mrp(random.nextDouble() * 100)
                .build();
    }

    public Claim generateRandomClaim(List<User> userData, List<Product> productData) {

        // Get random user and product
        User randomUser = userData.get(random.nextInt(userData.size()));

        // map of product id to product
        var productMap = productData.stream().collect(Collectors.toMap(Product::getId, it -> it));

        // Generate random products
        List<Claim.ProductDTO> products = IntStream.range(0, productData.size() / 3)
                .mapToObj(it -> productData.get(random.nextInt(productData.size())).getId())
                .distinct()
                .map(productMap::get)
                .map(it -> Claim.ProductDTO.builder()
                        .id(it.getId())
                        .name(it.getName())
                        .gstPercentage(it.getGstPercentage())
                        .uid(it.getUid())
                        .mrp(it.getMrp())
                        .description("Random Description " + random.nextInt(1000))
                        .quantity(random.nextInt(1000))
                        .build()).toList();

        // Generate random fields
        Claim.FormFieldValue field1 = Claim.FormFieldValue.builder()
                .key("invoice-number")
                .type("string")
                .value("RV" + random.nextInt(1000))
                .build();

        Claim.FormFieldValue field2 = Claim.FormFieldValue.builder()
                .key("credit-note")
                .type("string")
                .value("")
                .build();

        Claim.FormFieldValue field3 = Claim.FormFieldValue.builder()
                .key("claim-type")
                .type("string")
                .value("Breakage")
                .build();

        Claim.FormFieldValue field4 = Claim.FormFieldValue.builder()
                .key("reason-for-invalidation")
                .type("string")
                .value("None")
                .build();

        Claim.FormFieldValue field5 = Claim.FormFieldValue.builder()
                .key("claim-image")
                .type("image")
                .value("")
                .build();
        var fields = Arrays.asList(field1, field2, field3, field4, field5);


        // Build the claim
        var claimBuilder = Claim.builder()
                .requester(randomUser)
                .amount(Math.ceil(random.nextDouble() * 100))
                .createdDate(new Date())
                .status("New")
                .claimTemplateId(random.nextInt(1000) + "")
                .products(products)
                .fields(fields);

        if (random.nextBoolean()) {
            claimBuilder = claimBuilder
                    .status(STATUS_APPROVED)
                    .claimManager(userData.get(random.nextInt(userData.size())))
                    .evaluator(userData.get(random.nextInt(userData.size())));
        } else if (random.nextBoolean()) {
            claimBuilder = claimBuilder
                    .status(STATUS_DENIED)
                    .claimManager(userData.get(random.nextInt(userData.size())))
                    .evaluator(userData.get(random.nextInt(userData.size())));
        }

        if (random.nextBoolean()) {
            claimBuilder = claimBuilder
                    .status(STATUS_EVALUATOR_ASSIGNED)
                    .claimManager(userData.get(random.nextInt(userData.size())))
                    .evaluator(userData.get(random.nextInt(userData.size())));
        } else if (random.nextBoolean()) {
            claimBuilder = claimBuilder
                    .status(STATUS_APPROVAL_IN_PROGRESS)
                    .claimManager(userData.get(random.nextInt(userData.size())))
                    .evaluator(userData.get(random.nextInt(userData.size())));
        }


        return claimBuilder.build();
    }


}
