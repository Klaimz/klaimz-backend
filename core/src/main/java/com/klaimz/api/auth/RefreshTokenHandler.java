package com.klaimz.api.auth;

import com.klaimz.repo.LoginRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

@Singleton
public class RefreshTokenHandler implements RefreshTokenPersistence {


    @Inject
    private LoginRepository loginRepository;

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        if (event != null &&
                event.getRefreshToken() != null &&
                event.getAuthentication() != null &&
                event.getAuthentication().getName() != null) {
            String payload = event.getRefreshToken();
            System.out.println("Auth username" + event.getAuthentication().getName() + " Refresh token: " + payload);
            var loginData = loginRepository.findById(event.getAuthentication().getName());
            if (loginData.isPresent()) {
                var login = loginData.get();
                login.setToken(payload);
                loginRepository.update(login);
            }
        }
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        return Flux.create(emitter -> {
            var loginData = loginRepository.findByToken(refreshToken);

            if (loginData.isEmpty()) {
                emitter.error(new OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null));
                return;
            }

            if (!loginData.get().isActive()) {
                emitter.error(new OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null));
                return;
            }

            emitter.next(Authentication.build(loginData.get().getEmail()));
        }, FluxSink.OverflowStrategy.ERROR);
    }
}