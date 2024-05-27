package com.klaimz.model.api;

import com.klaimz.model.FilterableRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChartAnalyticsRequest implements FilterableRequest {
    private List<Filter> filters;
    private String groupBy;
    private String aggregateBy;
    private String aggregateType;
    private String chartType;
}