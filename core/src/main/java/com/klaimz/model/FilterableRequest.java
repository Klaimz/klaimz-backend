package com.klaimz.model;

import com.klaimz.model.api.Filter;

import java.util.List;

public interface FilterableRequest {
    List<Filter> getFilters();
}
