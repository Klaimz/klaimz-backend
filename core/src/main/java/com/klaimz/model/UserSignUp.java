package com.klaimz.model;


import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSignUp extends User {

    @NotBlank(message = "Password is required")
    private String password;

    public User convertToUser() {
        return User.builder()
                .displayName(getDisplayName())
                .email(getEmail())
                .phone(getPhone())
                .companyName(getCompanyName())
                .address(getAddress())
                .gstNumber(getGstNumber())
                .region(getRegion())
                .active(true)
                .roles(getRoles())
                .build();
    }
}
