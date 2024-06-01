package com.klaimz.model.api;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Introspected
public class GenericDto {

    @NotBlank
    private String body;
}
