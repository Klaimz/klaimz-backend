package com.klaimz.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSignUp extends User{

    private String password;

    public User convertToUser() {
        return User.builder()
                .displayName(this.getDisplayName())
                .email(this.getEmail())
                .phone(this.getPhone())
                .roles(this.getRoles())
                .active(true)
                .lastLoginDate(null)
                .id(null)
                .build();
    }
}
