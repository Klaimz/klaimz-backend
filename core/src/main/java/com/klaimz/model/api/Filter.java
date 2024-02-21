package com.klaimz.model.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Filter {
    private String field;
    private String value;
}
