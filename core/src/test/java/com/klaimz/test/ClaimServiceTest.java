package com.klaimz.test;


import com.klaimz.model.Claim;
import com.klaimz.model.Product;
import com.klaimz.model.User;
import com.klaimz.repo.ClaimRepository;
import com.klaimz.repo.ClaimTypeRepository;
import com.klaimz.repo.ProductRepository;
import com.klaimz.repo.UserRepository;
import com.klaimz.service.ClaimService;
import com.klaimz.util.Constants;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false, environments = "awesome")
@Getter
public class ClaimServiceTest extends BaseClaimTest {


    @Inject
    private ClaimService claimService;

    @Inject
    private ClaimRepository claimRepository;
    @Inject
    private ClaimTypeRepository claimTypeRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ProductRepository productRepository;
    private TestDataContainer testDataContainer;

    @BeforeEach
    public void setUp() {
        // Create data before each test
        this.testDataContainer = setUpData(MAX_CLAIMS_SZIE);
    }

    @Test
    @Order(0)
    public void testNoData() {
        claimRepository.deleteAll();
        var data = claimService.getAllClaims();
        assert data.isEmpty();
    }


    @Test
    @Order(2)
    public void testInsertUserData() {
        assert testDataContainer.getUsers().size() == MAX_USERS_SIZE;
    }

    @Test
    @Order(3)
    public void testInsertProductData() {
        assert testDataContainer.getProducts().size() == MAX_PRODUCTS_SIZE;
    }


    @Test
    @Order(4)
    public void testInsertClaimData() {
        assert testDataContainer.getClaims().size() == MAX_CLAIMS_SZIE;
    }

    @Test
    @Order(5)
    public void shouldThrowExceptionWhenClaimIsNull() {
        assertThrows(NullPointerException.class, () -> claimService.createClaim(null, "requesterId"));
    }

    @Test
    @Order(6)
    public void shouldThrowExceptionWhenClaimIdIsNull() {
        assertThrows(NullPointerException.class, () -> claimService.getClaimById(null));
    }

    @Test
    @Order(7)
    public void shouldThrowExceptionWhenClaimIdIsNotFound() {
        String nonExistentId = "nonExistentId";
        assertThrows(IllegalArgumentException.class, () -> claimService.getClaimById(nonExistentId));
    }

    @Test
    @Order(8)
    public void shouldCreateClaimSuccessfully() {
        var data = setUpData(10);
        Claim claim = generateRandomClaim(data.getUsers(), data.getProducts());
        var user = data.getUsers().get(0);
        Claim createdClaim = claimService.createClaim(claim, user.getId());
        assertNotNull(createdClaim);
        assertEquals(user.getId(), createdClaim.getRequester().getId());
    }

    @Test
    @Order(9)
    public void shouldUpdateClaimSuccessfully() {
        var data = setUpData(10);
        Claim claim = generateRandomClaim(data.getUsers(), data.getProducts());
        var user = data.getUsers().get(0);
        Claim createdClaim = claimService.createClaim(claim, user.getId());
        createdClaim.setStatus(Constants.STATUS_CM_ASSIGNED);
        Claim updatedClaim = claimService.updateClaim(createdClaim, true);
        assertNotNull(updatedClaim);
        assertEquals(Constants.STATUS_CM_ASSIGNED, updatedClaim.getStatus());
    }

    @Test
    @Order(10)
    public void shouldAddCommentSuccessfully() {
        var data = setUpData(10);
        var user = data.getUsers().get(0);
        Claim createdClaim =  claimService.createClaim(
                generateRandomClaim(data.getUsers(), data.getProducts()),user.getId());

        Claim updatedClaim = claimService.addComment(createdClaim.getId(), "Test Comment", user.getId());
        assertNotNull(updatedClaim);
        assertTrue(updatedClaim.getUpdates().stream().anyMatch(comment -> comment.getComment().equals("Test Comment")));
    }

    @Test
    @Order(11)
    public void shouldUpdateStatusSuccessfully() {
        var data = setUpData(10);
        var user = data.getUsers().get(0);
        Claim createdClaim =  claimService.createClaim(
                generateRandomClaim(data.getUsers(), data.getProducts()),user.getId());

        Claim updatedClaim = (Claim) claimService.updateStatus(createdClaim.getId(), "Updated Status", "Test User");
        assertNotNull(updatedClaim);
        assertEquals("Updated Status", updatedClaim.getStatus());
    }

}
