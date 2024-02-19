package com.klaimz.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Generated
    @Id
    private String id;
    private String description;
}