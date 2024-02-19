package com.klaimz.service;

import com.klaimz.model.LoginData;
import com.klaimz.model.User;
import com.klaimz.model.UserSignUp;
import com.klaimz.repo.LoginRepository;
import com.klaimz.repo.UserRepository;
import com.klaimz.util.HashUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private LoginRepository loginRepository;

    public static String VERIFIED = "verified";

    private Function<User, String> emptyCheck(Function<User, String> generator, String message) {
        return user -> {
            var value = generator.apply(user);
            if (value == null || value.isBlank()) {
                return message;
            }
            return VERIFIED;
        };
    }

    List<Function<User, String>> validators = List.of(
            emptyCheck(User::getDisplayName, "User must have a display name"),
            emptyCheck(User::getEmail, "User must have an email"),
            emptyCheck(User::getPhone, "User must have a phone number"),
            user -> {
                var roles = user.getRoles();
                if (roles == null || roles.isEmpty()) {
                    return "User must have at least one role";
                }
                return VERIFIED;
            },
            user -> {
                var roles = user.getRoles();
                for (var role : roles) {
                    if (role.getId() == null || role.getId().isBlank()) {
                        return "Role must have an id";
                    }
                    if (role.getDescription() == null || role.getDescription().isBlank()) {
                        return "Role must have a description";
                    }
                }
                return VERIFIED;
            }
    );


    public User createUser(UserSignUp signUp) throws NoSuchAlgorithmException {
        for (var validator : validators) {
            var result = validator.apply(signUp);
            if (!result.equals(VERIFIED)) {
                throw new IllegalArgumentException(result);
            }
        }

        var createdUser = userRepository.save(signUp.convertToUser());

        loginRepository.save(LoginData.builder()
                .email(signUp.getEmail())
                .passwordHash(HashUtils.hash(signUp.getPassword()))
                .build());

        return createdUser;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
}
