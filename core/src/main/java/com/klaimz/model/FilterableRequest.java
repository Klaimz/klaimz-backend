package com.klaimz.model;

import com.klaimz.model.api.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface FilterableRequest {
    List<Filter> getFilters();

    default List<String> getFilterFields(){
        if (getFilters() == null) {
            return new ArrayList<>();
        }
        return getFilters().stream().map(Filter::getField)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    List<String> getFields();
}
