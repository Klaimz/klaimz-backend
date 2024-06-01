package com.klaimz.model;

import io.micronaut.data.annotation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
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

    @Email(message = "Email should be valid")
    private String email;
    private String phone;


    @NotBlank(message = "User must have a company name")
    private String companyName;


    @NotBlank(message = "User must have a company address")
    private String address;
    private String gstNumber;
    private String region;

    @DateUpdated
    private Date lastLoginDate;

    @DateCreated
    private Date createdDate;
    private boolean active;

    @NotEmpty(message = "User must have roles")
    private List<String> roles;
}