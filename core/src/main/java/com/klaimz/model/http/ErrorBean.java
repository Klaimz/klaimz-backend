package com.klaimz.model.http;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorBean implements MessageBean {
    private String message;
    private String stackTrace; // optional

    @Override
    public Object getData() {
        return null;
    }
}
