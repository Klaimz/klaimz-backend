package com.klaimz.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@Serdeable
public class ClaimUpdate {

    @Generated
    @Id
    private String id;
    private String comment;
    private String status;
    private String updateDate;
    private User updatedBy;
}