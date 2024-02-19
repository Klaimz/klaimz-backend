package com.klaimz.model;


import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormField {

    @Generated
    @Id
    private String id;
    private String type;
    private String regex;
    private String defaultValue;
    private String upperRange;
    private String lowerRange;
    private String defaultName;
}
