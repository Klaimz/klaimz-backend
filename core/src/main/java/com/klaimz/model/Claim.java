package com.klaimz.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;

import java.util.List;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Claim {

    @Generated
    @Id
    private String id;
    private String description;
    private String amount;
    private String createdDate;
    private String status;
    private List<FormFieldValue> fields;
    private long updateDate;
    private User requester;
    private Company vendor;
    private List<ClaimUpdate> updates;
    private String resolutionDate;
    private User claimManager;
    private String claimTemplateId;



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FormFieldValue {
        private String type;
        private String value;
    }
}