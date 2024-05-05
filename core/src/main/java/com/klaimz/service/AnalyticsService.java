package com.klaimz.service;


import com.klaimz.model.ChartEntry;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.repo.AnalyticsRepository;
import io.micronaut.data.mongodb.operations.options.MongoAggregationOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.*;

import static com.klaimz.util.Constants.CHART_TYPE_BAR;
import static com.klaimz.util.Constants.CHART_TYPE_PIE;
import static com.klaimz.util.MongoUtils.*;

@Singleton
public class AnalyticsService {
    @Inject
    AnalyticsRepository analyticsRepository;

    public List<ChartEntry> getChartAnalytics(ChartAnalyticsRequest request) {
        var pipeline = createAnalyticsPipeline(request);

        MongoAggregationOptions aggregationOptions = new MongoAggregationOptions();

        var data = analyticsRepository.findAll(pipeline, aggregationOptions);

        return switch (request.getChartType()) {
            case CHART_TYPE_PIE -> convertToPie(data);
            default -> data;
        };
    }

    private List<ChartEntry> convertToPie(List<ChartEntry> data) {
        var total = data.stream().mapToDouble(ChartEntry::getY).sum();
        for (var entry : data) {
            entry.setY((entry.getY() * 100.0) / total);
        }
        return data;
    }

}
