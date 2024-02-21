package com.klaimz.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.util.Date;
import java.util.List;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue
    private String id;
    private String description;
    private String amount;

    @DateCreated
    private Date createdDate;
    private String status;
    private List<FormFieldValue> fields;

    @DateUpdated
    private Date updateDate;
    private String requesterUserId;
    private String requesterCompanyId;
    private String vendorCompanyId;

    private List<ClaimUpdate> updates;
    private String resolutionDate;

    private String claimManagerId;
    private String claimTemplateId;

    @Data
    @Introspected
    @Serdeable
    @Getter
    public static class FormFieldValue {
        private String key;
        private String type;
        private String value;
    }
}