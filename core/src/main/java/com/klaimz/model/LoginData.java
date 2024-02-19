package com.klaimz.model;


import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginData {

    @Id
    private String email;
    private String passwordHash;

    @Builder.Default
    private boolean active = true;
    private String token;


    @DateUpdated
    private long lastLogin;

}
