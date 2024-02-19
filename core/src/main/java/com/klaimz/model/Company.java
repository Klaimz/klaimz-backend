package com.klaimz.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.*;


@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company {

    @Generated
    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String registrationNumber;
    private User pointOfContact;
}