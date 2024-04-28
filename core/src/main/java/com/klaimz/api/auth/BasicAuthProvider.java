package com.klaimz.api.auth;

import com.klaimz.model.LoginData;
import com.klaimz.model.User;
import com.klaimz.repo.LoginRepository;
import com.klaimz.service.UserService;
import com.klaimz.util.HashUtils;
import com.nimbusds.jose.shaded.gson.Gson;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class BasicAuthProvider implements AuthenticationProvider<HttpRequest<?>> {

    @Inject
    private UserService userService;

    @Inject
    private LoginRepository loginRepository;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authReq) {

        final String email = authReq.getIdentity().toString();
        final String password = authReq.getSecret().toString();

        if (!httpRequest.getPath().equals("/user/login")) {
            return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.CUSTOM));
        }

        Optional<LoginData> optionalLoginData = loginRepository.findByEmail(email);

        if (optionalLoginData.isEmpty()) {
            return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
        }
        var loginData = optionalLoginData.get();
        if (!loginData.isActive()) {
            return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.USER_DISABLED));
        }


        try {
            var passwordHash = HashUtils.hash(password);
            if (!passwordHash.equals(loginData.getPasswordHash())) {
                return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
            }

            Optional<User> optionalUser = userService.getUserById(loginData.getId());

            if (optionalUser.isEmpty()) {
                return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
            }

            userService.updateLoginDate(optionalUser.get().getId());
            var userMap = Map.of("user",(Object) optionalUser.get());

            return Flux.create(emitter -> {
                emitter.next(AuthenticationResponse.success(optionalUser.get().getId(), userMap));
                emitter.complete();
            });

        } catch (NoSuchAlgorithmException e) {
            return Mono.just(new AuthenticationFailed(AuthenticationFailureReason.UNKNOWN));
        }
    }
}