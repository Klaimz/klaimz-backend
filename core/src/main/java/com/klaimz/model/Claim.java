package com.klaimz.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.OneToOne;
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
    private String amount;

    @DateCreated
    private Date createdDate;

    private String status;

    private List<ProductDTO> products;
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

    @Data
    @Introspected
    @Serdeable
    @Getter
    public static class ProductDTO {
        private String id;
        private String description;
        private int quantity;

//        auto fetch field values from product table
        private double mrp;
        private String name;
        private String uid;
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