package com.klaimz.model.api;

import com.klaimz.model.FilterableRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class TopKClaimRequest implements FilterableRequest {
    private List<Filter> filters;
    private String target;
    private String sortBy;
    private int limit;


    @Override
    public List<String> getFields() {
        var fields = getFilterFields();

        if (target != null) {
            fields.add(target);
        }
        if (sortBy != null) {
            fields.add(sortBy);
        }

        return fields;
    }
}
