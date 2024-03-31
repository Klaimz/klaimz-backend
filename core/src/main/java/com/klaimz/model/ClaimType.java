package com.klaimz.model;


import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimType {

    @Id
    @GeneratedValue
    private String id;
    private String typeName;
}
