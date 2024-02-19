package com.klaimz.api;

import com.klaimz.service.UserService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import java.security.Principal;

import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/user")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class UserController {

    @Inject
    private UserService userService;

    @Get("/me")
    public String me(@NonNull Principal principal) {

        return principal.getName();
    }
}
