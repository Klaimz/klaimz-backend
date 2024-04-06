package com.klaimz.api;

import com.klaimz.model.api.Filter;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.UserService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import java.security.Principal;

import static com.klaimz.util.HttpUtils.badRequest;
import static com.klaimz.util.HttpUtils.success;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/user")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class UserController {

    @Inject
    private UserService userService;

    @Get("/me")
    public HttpResponse<MessageBean> me(@NonNull Principal principal) {
        var userId = principal.getName();
        var user =  userService.getUserById(userId);

        if (user.isEmpty()) {
            return badRequest("Current User profile not found");
        }

        return success(user, "Current User profile");
    }

    @Get("/{id}")
    public HttpResponse<MessageBean> getUserById(@NonNull String id) {
        var user =  userService.getUserById(id);

        if (user.isEmpty()) {
            return badRequest("User not found");
        }

        return success(user, "User found");
    }

    @Get("/all")
    public HttpResponse<MessageBean> getAllUsers() {
        var result = userService.getAllUsers();

        return success(result, "All users");
    }

    @Post("/search")
    public HttpResponse<MessageBean> search(@Body Filter filter) {
        var result = userService.findByField(filter);

        return success(result, "User search result");
    }
}
