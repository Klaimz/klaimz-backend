package com.klaimz.util;

import com.klaimz.model.http.ErrorBean;
import com.klaimz.model.http.MessageBean;
import com.klaimz.model.http.SuccessBean;
import io.micronaut.http.HttpResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

public class HttpUtils {

    // bad request
    public static HttpResponse<MessageBean> badRequest(String message) {
        return HttpResponse
                .badRequest(ErrorBean.builder().message(message).build());
    }

    // print stack trace to string of an exception
    public static String stackTrace(Exception e){
        var stringWriter  = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return stringWriter.toString();
    }
    // handle exception
    public static HttpResponse<ErrorBean> handleException(Exception e) {
        e.printStackTrace();
        return HttpResponse.serverError(ErrorBean.builder()
                .message(e.getMessage())
                .stackTrace(stackTrace(e)).build());
    }
    // not found

    public static HttpResponse<MessageBean> notFound(String message) {
        return HttpResponse.notFound(ErrorBean.builder().message(message).build());
    }

    // success response
    public static <T> HttpResponse<MessageBean> success(T data,String message) {
        return HttpResponse.ok(SuccessBean.builder()
                .data(data)
                .message(message).build());
    }

    public  static HttpResponse<MessageBean> success(String message) {
        return HttpResponse.ok(SuccessBean.builder()
                .data(null)
                .message(message).build());
    }

}
