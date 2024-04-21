package com.klaimz.model;


import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MappedEntity( "claim")
public class ChartEntry {

    @Id
    private String _id;
    private Double yvalue;
    private String xvalue;
}
