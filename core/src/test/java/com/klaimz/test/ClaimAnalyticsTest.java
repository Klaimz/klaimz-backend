package com.klaimz.test;

import com.klaimz.model.ChartEntry;
import com.klaimz.model.Claim;
import com.klaimz.model.User;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import com.klaimz.model.api.TopKClaimRequest;
import com.klaimz.repo.*;
import com.klaimz.service.AnalyticsService;
import com.klaimz.service.ClaimService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.klaimz.util.Constants.*;
import static com.klaimz.util.MongoUtils.convertToPie;
import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;


@MicronautTest(startApplication = false, environments = "awesome")
@Getter
public class ClaimAnalyticsTest extends BaseClaimTest {


    @Inject
    private AnalyticsRepository analyticsRepository;

    @Inject
    private AnalyticsService analyticsService;

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
        getClaimRepository().deleteAll();
        getClaimTypeRepository().deleteAll();
        getUserRepository().deleteAll();
        getProductRepository().deleteAll();
        getAnalyticsRepository().deleteAll();
        this.testDataContainer = null;
        this.testDataContainer = setUpData(MAX_CLAIMS_SZIE);
    }


    @Test
    public void testGetTopKClaims() {
        // pick a random user
        var user = testDataContainer.getUsers().get(random.nextInt(testDataContainer.getUsers().size()));


        TopKClaimRequest request = new TopKClaimRequest();
        request.setFilters(List.of(new Filter("requester.displayName", user.getDisplayName())));
        request.setTarget("requester.displayName");
        request.setSortBy("amount");
        request.setLimit(3);

        // Calculate expected analytics data manually
        List<ChartEntry> expectedData = testDataContainer.getClaims().stream()
                .filter(claim -> user.getDisplayName().equals(claim.getRequester().getDisplayName()))
                .sorted(Comparator.comparingDouble(Claim::getAmount).reversed())
                .limit(3)
                .map(claim -> ChartEntry.builder()
                        .x(claim.getRequester().getDisplayName())
                        .y(claim.getAmount())
                        .build())
                .toList();

        // Call the getTopKClaims method from the analyticsService
        List<ChartEntry> actualData = analyticsService.getTopKClaims(request);

        // Compare the returned data from the service with the manually calculated data
        assertEquals(expectedData.size(), actualData.size());
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), actualData.get(i).getX());
            assertEquals(expectedData.get(i).getY(), actualData.get(i).getY(), 0.001);
        }
    }

    @Test
    public void testConvertToPie() {
        // Create a list of ChartEntry objects
        List<ChartEntry> data = Arrays.asList(
                ChartEntry.builder().x("A").y(100.0).build(),
                ChartEntry.builder().x("B").y(200.0).build(),
                ChartEntry.builder().x("C").y(300.0).build(),
                ChartEntry.builder().x("D").y(400.0).build()
        );

        // Call the convertToPie method
        List<ChartEntry> result = convertToPie(data);

        // Verify that the percentages sum up to 100 (or very close to it)
        double sum = result.stream().mapToDouble(ChartEntry::getY).sum();
        assertEquals(100, sum, 0.001);
    }

    @Test
    public void testChartAnalyticsRequesterStatusVsAmount() {
        String displayName = this.testDataContainer.getUsers().get(0).getDisplayName();
        // Create a ChartAnalyticsRequest object
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType(CHART_TYPE_PIE);
        request.setFilters(List.of(new Filter("requester.displayName", displayName)));
        request.setGroupBy("status");
        request.setAggregateBy("amount");
        request.setAggregateType("sum");

        // Call the getChartAnalytics method
        List<ChartEntry> result = analyticsService.getChartAnalytics(request);

        // Verify that the returned list is not empty
        assertFalse(result.isEmpty());

        // Verify that the percentages sum up to 100 (or very close to it)
        double sum = result.stream().mapToDouble(ChartEntry::getY).sum();
        assertEquals(100, sum, 0.01);

        // Calculate the expected data manually
        List<ChartEntry> chartData = testDataContainer.getClaims()
                .stream()
                .filter(claim -> claim.getRequester().getDisplayName().equals(displayName))
                .collect(groupingBy(Claim::getStatus, summingDouble(Claim::getAmount)))
                .entrySet().stream()
                .map(entry -> ChartEntry.builder().x(entry.getKey()).y(entry.getValue()).build())
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        //  convert to percentage
        var expectedData = convertToPie(chartData);

        result = result.stream()
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        assertEquals(expectedData.size(), result.size());
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), result.get(i).getX());
            assertEquals(expectedData.get(i).getY(), result.get(i).getY(), 0.001);
        }
    }

    @Test
    public void testChartAnalyticsRequesterDisplayNameVsAmount() {

        // Create a ChartAnalyticsRequest object
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType(CHART_TYPE_BAR);
        request.setGroupBy("requester.displayName");
        request.setAggregateBy("amount");
        request.setAggregateType("sum");

        // Call the getChartAnalytics method
        List<ChartEntry> result = analyticsService.getChartAnalytics(request);

        // Verify that the returned list is not empty
        assertFalse(result.isEmpty());

        // Verify that the percentages sum up to 100 (or very close to it)
        double resultSum = result.stream().mapToDouble(ChartEntry::getY).sum();

        // Calculate the expected data manually
        List<ChartEntry> expectedData = testDataContainer.getClaims()
                .stream()
                .collect(groupingBy(claim -> claim.getRequester().getDisplayName(), summingDouble(Claim::getAmount)))
                .entrySet().stream()
                .map(entry -> ChartEntry.builder()._id(entry.getKey()).x(entry.getKey()).y(entry.getValue()).build())
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        //  convert to percentage
        var expectedSum = expectedData.stream().mapToDouble(ChartEntry::getY).sum();

        assertEquals(expectedSum, resultSum, 0.01);


        assertEquals(expectedData.size(), result.size());
        result = result.stream()
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), result.get(i).getX());
            assertEquals(expectedData.get(i).getY(), result.get(i).getY(), 0.001);
        }
    }

    @Test
    public void testChartAnalyticsRequesterVsAmount() {
        String productName = this.testDataContainer.getProducts().get(0).getName();

        // Create a ChartAnalyticsRequest object
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType(CHART_TYPE_BAR);
        request.setFilters(List.of(new Filter("products.name", productName)));
        request.setGroupBy("requester.displayName");
        request.setAggregateBy("amount");
        request.setAggregateType("avg");

        // Call the getChartAnalytics method
        List<ChartEntry> result = analyticsService.getChartAnalytics(request);


        // Verify that the percentages sum up to 100 (or very close to it)
        double sum = result.stream().mapToDouble(ChartEntry::getY).sum();

        // Calculate the expected data manually
        List<ChartEntry> expectedData = testDataContainer.getClaims()
                .stream()
                .filter(claim -> claim.getProducts().stream().anyMatch(product -> product.getName().equals(productName)))
                .collect(groupingBy(claim -> claim.getRequester().getDisplayName(), averagingDouble(Claim::getAmount)))
                .entrySet().stream()
                .map(entry -> ChartEntry.builder().x(entry.getKey())._id(entry.getKey()).y(entry.getValue()).build())
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        // verify size of result
        assertEquals(expectedData.size(), result.size());

        var expectedSum = expectedData.stream().mapToDouble(ChartEntry::getY).sum();
        assertEquals(expectedSum, sum, 0.01);


        result = result.stream()
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        assertEquals(expectedData.size(), result.size());
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), result.get(i).getX());
            assertEquals(expectedData.get(i).getY(), result.get(i).getY(), 0.01);
        }
    }


    @Test
    public void testChartAnalyticsRequesterVsMinAmount() {
        String productName = this.testDataContainer.getProducts().get(0).getName();

        // Create a ChartAnalyticsRequest object
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType(CHART_TYPE_BAR);
        request.setFilters(List.of(
                new Filter("products.name", productName),
                new Filter("status", STATUS_APPROVED)
        ));
        request.setGroupBy("requester._id");
        request.setAggregateBy("amount");
        request.setAggregateType("min");

        // Call the getChartAnalytics method
        List<ChartEntry> result = analyticsService.getChartAnalytics(request);


        // Verify that the percentages sum up to 100 (or very close to it)
        var sum = result.stream().mapToDouble(ChartEntry::getY).sum();


        // Calculate the expected data manually
        List<ChartEntry> expectedData = testDataContainer.getClaims()
                .stream()
                .filter(claim -> claim.getProducts().stream().anyMatch(
                        product ->
                                product.getName().equals(productName)) &&
                                 claim.getStatus().equals(STATUS_APPROVED))
                .collect(groupingBy(claim -> claim.getRequester().getId(),
                        collectingAndThen(minBy(Comparator.comparingDouble(Claim::getAmount)),
                                optionalClaim -> optionalClaim.get().getAmount())))
                .entrySet().stream()
                .map(entry -> ChartEntry.builder().x(entry.getKey()).y(entry.getValue()).build())
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        assertEquals(expectedData.size(), result.size());

        var expectedSum = expectedData.stream().mapToDouble(ChartEntry::getY).sum();

        assertEquals(expectedSum, sum, 0.001);

        result = result.stream()
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        assertEquals(expectedData.size(), result.size());
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), result.get(i).getX());
            assertEquals(expectedData.get(i).getY(), result.get(i).getY(), 0.001);
        }
    }

    @Test
    public void testChartAnalyticsRequesterVsMaxAmount() {
        String productName = this.testDataContainer.getProducts().get(0).getName();
        User user = this.testDataContainer.getUsers().get(0);

        // Create a ChartAnalyticsRequest object
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType(CHART_TYPE_PIE);
        request.setFilters(List.of(
                new Filter("products.id", productName),
                new Filter("status", STATUS_DENIED),
                new Filter("requester._id", user.getId())
        ));
        request.setGroupBy("requester.displayName");
        request.setAggregateBy("status");
        request.setAggregateType("count");

        // Call the getChartAnalytics method
        List<ChartEntry> result = analyticsService.getChartAnalytics(request);


        // Verify that the percentages sum up to 100 (or very close to it)
        var sum = result.stream().mapToDouble(ChartEntry::getY).sum();


        // Calculate the expected data manually
        List<ChartEntry> chartData = testDataContainer.getClaims()
                .stream()
                .filter(claim -> claim.getProducts().stream().anyMatch(
                        product ->
                                product.getName().equals(productName)) &&
                                 claim.getStatus().equals(STATUS_DENIED) &&
                                    claim.getRequester().getId().equals(user.getId()))
                .collect(groupingBy(claim -> claim.getRequester().getId(),
                        collectingAndThen(maxBy(Comparator.comparingDouble(Claim::getAmount)),
                                optionalClaim -> optionalClaim.get().getAmount())))
                .entrySet().stream()
                .map(entry -> ChartEntry.builder().x(entry.getKey()).y(entry.getValue()).build())
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        var expectedData = convertToPie(chartData);

        assertEquals(expectedData.size(), result.size());

        var expectedSum = expectedData.stream().mapToDouble(ChartEntry::getY).sum();

        assertEquals(expectedSum, sum, 0.001);

        result = result.stream()
                .sorted(Comparator.comparing(ChartEntry::getX))
                .toList();

        assertEquals(expectedData.size(), result.size());
        for (int i = 0; i < expectedData.size(); i++) {
            assertEquals(expectedData.get(i).getX(), result.get(i).getX());
            assertEquals(expectedData.get(i).getY(), result.get(i).getY(), 0.001);
        }
    }

    @Test
    public void testGetTopKClaimsWithEmptyRequest() {
        // Create an empty request
        TopKClaimRequest request = new TopKClaimRequest();

        // Call the getTopKClaims method with the empty request
        assertThrows(ConstraintViolationException.class, () -> analyticsService.getTopKClaims(request));
    }

    @Test
    public void testGetChartAnalyticsWithEmptyRequest() {
        // Create an empty request
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();

        // Call the getChartAnalytics method with the empty request and assert that it throws a ConstraintViolationException
        assertThrows(ConstraintViolationException.class, () -> analyticsService.getChartAnalytics(request));
    }

    @Test
    public void testGetTopKClaimsWithInvalidInput() {
        // Create an invalid request
        TopKClaimRequest request = new TopKClaimRequest();
        request.setTarget("nonExistentUser");

        // Call the getTopKClaims method with the invalid request and assert that it throws a ConstraintViolationException
        assertThrows(ConstraintViolationException.class, () -> analyticsService.getTopKClaims(request));
    }

    @Test
    public void testGetChartAnalyticsWithInvalidInput() {
        // Create an invalid request
        ChartAnalyticsRequest request = new ChartAnalyticsRequest();
        request.setChartType("invalidChartType");
        request.setAggregateType("invalidAggregateType");

        // Call the getChartAnalytics method with the invalid request
        assertThrows(ConstraintViolationException.class, () -> analyticsService.getChartAnalytics(request));
    }
}
