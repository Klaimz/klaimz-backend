package com.klaimz.model;


import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends Claim.ProductDTO {

    @Id
    @GeneratedValue
    private String id;
    private String uid;
    private double gstPercentage;
}
