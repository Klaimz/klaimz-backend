package com.klaimz.model.api;

import com.klaimz.model.FilterableRequest;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@Introspected
public class TopKClaimRequest implements FilterableRequest {

    @Valid
    private List<Filter> filters;

    @NotBlank
    private String target;

    @NotBlank
    private String sortBy;

    @Positive
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
