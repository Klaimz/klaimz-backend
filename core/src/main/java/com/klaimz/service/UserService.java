package com.klaimz.service;

import com.klaimz.model.LoginData;
import com.klaimz.model.User;
import com.klaimz.model.UserSignUp;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.LoginRepository;
import com.klaimz.repo.RoleRepository;
import com.klaimz.repo.UserRepository;
import com.klaimz.util.HashUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.klaimz.util.StringUtils.VERIFIED;
import static com.klaimz.util.StringUtils.emptyCheck;

@Singleton
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private LoginRepository loginRepository;

    @Inject
    private RoleRepository roleRepository;

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
                .id(createdUser.getId())
                .email(signUp.getEmail())
                .passwordHash(HashUtils.hash(signUp.getPassword()))
                .build());

        return createdUser;
    }

    public void updateLoginDate(String id) {
        getUserById(id).ifPresent(user -> {
            user.setLastLoginDate(new Date());
            userRepository.update(user);
        });
    }

    public List<User> findByField(Filter filter) {

        if (filter.getField() == null || filter.getValue() == null) {
            throw new IllegalArgumentException("Field and value must be provided");
        }

        if (filter.getField().equals("roles")) {
            return userRepository.findByRoleIn(filter.getValue());
        }

        return userRepository.findAll(UserRepository.Specification.findByField(filter.getField(), filter.getValue()));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    private Optional<LoginData>  getLoginDataById(String id) {
        return loginRepository.findById(id);
    }

    public Optional<LoginData> getLoginDataByToken(String token) {
        return loginRepository.findByToken(token);
    }

    public void updateToken(String id,String token) {
        var loginData = getLoginDataById(id);
        if (loginData.isPresent()) {
            var login = loginData.get();
            login.setToken(token);
            loginRepository.update(login);
        }
    }

    public void getAllUsers() {
       var all = userRepository.findAll();
       all.forEach(System.out::println);
    }
}
