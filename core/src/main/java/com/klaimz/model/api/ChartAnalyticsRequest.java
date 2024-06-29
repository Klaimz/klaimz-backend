package com.klaimz.model.api;

import com.klaimz.model.FilterableRequest;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Introspected
public class ChartAnalyticsRequest implements FilterableRequest {

    private List<@Valid @Filter.ValidatedFilter Filter> filters;

    @NotBlank
    private String groupBy;
    @NotBlank
    private String aggregateBy;
    @NotBlank
    private String aggregateType;
    @NotBlank
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