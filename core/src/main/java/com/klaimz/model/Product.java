package com.klaimz.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class Product {
    @Id
    @GeneratedValue
    private String id;

    @Positive(message = "Product must have a valid price")
    private double mrp;

    @NotBlank(message = "Product must have a name")
    private String name;


    @NotBlank(message = "Product must have a UID")
    private String uid;

    @Positive(message = "Product must have a valid GST percentage")
    @DecimalMax(value = "28", message = "Product GST percentage must be between 1 and 28")
    private double gstPercentage;
}
