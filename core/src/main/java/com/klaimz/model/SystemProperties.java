package com.klaimz.model;


import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemProperties {

    @Id
    private String id;
    private String value;
}
