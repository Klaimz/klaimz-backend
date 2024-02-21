package com.klaimz.model;

import io.micronaut.data.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private String id;
    private String displayName;
    private String email;
    private String phone;

    @DateUpdated
    private Date lastLoginDate;

    @DateCreated
    private Date createdDate;
    private boolean active;
    private List<String> roles;
}