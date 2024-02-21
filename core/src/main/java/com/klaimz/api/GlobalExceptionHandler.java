package com.klaimz.api;

import com.klaimz.model.http.ErrorBean;
import com.klaimz.model.http.MessageBean;
import com.klaimz.util.HttpUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<Exception, HttpResponse<MessageBean>> {
    @Override
    public HttpResponse<MessageBean> handle(HttpRequest request, Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return HttpUtils.badRequest(exception.getMessage());
        }


        return HttpUtils.handleException(exception);
    }
}
