package com.klaimz.model.api;

import com.klaimz.model.FilterableRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ChartAnalyticsRequest implements FilterableRequest {
    private List<Filter> filters;
    private String groupBy;
    private String aggregateBy;
    private String aggregateType;
    private String chartType;


    @Override
    public List<String> getFields() {
        var fields = getFilterFields();

        if (groupBy != null) {
            fields.add(groupBy);
        }
        if (aggregateBy != null) {
            fields.add(aggregateBy);
        }

        return fields;
    }
}