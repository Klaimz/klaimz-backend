package com.klaimz.api;


import com.klaimz.model.UserSignUp;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.UserService;
import com.klaimz.util.HttpUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import java.security.NoSuchAlgorithmException;

import static com.klaimz.util.HttpUtils.success;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/user/signup")
@Secured(IS_AUTHENTICATED)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ExecuteOn(TaskExecutors.BLOCKING)
public class SignUpController {

    @Inject
    private UserService userService;

    @Post
    public HttpResponse<MessageBean> registerUser(@Body UserSignUp userSignUp) throws NoSuchAlgorithmException {
        var user = userService.createUser(userSignUp);
        if (user == null) {
            return HttpUtils.badRequest("Unable to signup user");
        }

        return success(user, "User created successfully");
    }

}
