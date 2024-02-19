package com.klaimz.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimUpdate {

    @Generated
    @Id
    private String id;
    private String comment;
    private String status;
    private String updateDate;
    private User updatedBy;
}