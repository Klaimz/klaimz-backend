package com.klaimz.model;


import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class ClaimType {

    @Id
    @GeneratedValue
    private String id;

    @NotBlank
    private String typeName;
}
