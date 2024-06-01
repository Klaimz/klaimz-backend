package com.klaimz.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
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

    @Positive (message = "Claim must have a valid amount")
    private double amount;

    @DateCreated
    private Date createdDate;

    @NotBlank(message = "Claim must have a status")

    private String status;

    @NotEmpty(message = "Claim must have products")
    @Valid
    private List<ProductDTO> products;

    @NotEmpty(message = "Claim must have fields")
    @Valid
    private List<FormFieldValue> fields;

    @DateUpdated
    private Date updateDate;


    @OneToOne
    private User requester;

    @OneToOne
    private User evaluator;

    @OneToOne
    private User claimManager;


    @Builder.Default
    private ArrayList<ClaimUpdate> updates = new ArrayList<>();


    @NotBlank(message = "Claim must have a claim template id")
    private String claimTemplateId;

    @Data
    @Introspected
    @Serdeable
    @Getter
    @Builder
    public static class FormFieldValue {
        @NotBlank(message = "Field must have a key")
        private String key;
        @NotBlank(message = "Field must have a type")
        private String type;
        private String value;
    }

    @Data
    @Introspected
    @Serdeable
    @Getter
    @Builder
    public static class ProductDTO {

        @NotBlank(message = "Product must have a valid id")
        private String id;
        @NotBlank(message = "Product must have a valid description")
        private String description;

        @Positive(message = "Product must have a valid quantity")
        private int quantity;

        @Positive(message = "Product must have a valid mrp")
        private double mrp;


        @NotBlank(message = "Product must have a valid name")
        private String name;
        private String uid;


        @Positive(message = "Product must have a valid GST percentage")
        @DecimalMax(value = "28", message = "Product GST percentage must be between 1 and 28")
        private double gstPercentage;
    }


    @Data
    @Introspected
    @Serdeable
    @Getter
    @Builder
    public static class ClaimUpdate {

        public static final String TYPE_COMMENT = "comment";
        public static final String TYPE_STATUS = "status";

        private String id;
        private String type;
        private String comment;
        private String newValue;
        private String oldValue;
        private long  time;
        private String user;
    }

    public void addComment(String comment,String user) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        if (updates == null) {
            updates = new ArrayList<>();
        }

       var update = ClaimUpdate.builder()
               .comment(comment)
               .time(System.currentTimeMillis())
               .user(user)
                .type(ClaimUpdate.TYPE_COMMENT)
               .build();
       updates.add(update);
    }

    public void updateStatus(String status,String user) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        if (status.equals(this.status)) {
            throw new IllegalArgumentException("Status is already " + status);
        }
        if (updates == null) {
            updates = new ArrayList<>();
        }
        var update = ClaimUpdate.builder()
                .newValue(status)
                .time(System.currentTimeMillis())
                .user(user)
                .oldValue(this.status)
                .comment("Status updated to " + status)
                .newValue(status)
                .type(ClaimUpdate.TYPE_STATUS)
                .build();
        this.status = status;
        updates.add(update);
    }

    public void updateField(String key, String value) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        var field = fields.stream()
                .filter(f -> f.getKey().equals(key))
                .findFirst();
        if (field.isEmpty()) {
            throw new IllegalArgumentException("Field not found");
        }
        field.get().setValue(value);
    }

    public FormFieldValue getField(String key) {
        if (fields == null) {
            throw  new IllegalArgumentException("No Fields found");
        }
        var field = fields.stream()
                .filter(f -> f.getKey().equals(key))
                .findFirst();

        if (field.isEmpty()) {
            throw new IllegalArgumentException("Field not found");
        }

        return field.get();
    }
}