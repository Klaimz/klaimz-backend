package com.klaimz.model.http;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessBean implements MessageBean {
    private String message;
    private Object data;

    @Override
    public String getStackTrace() {
        return null;
    }
}
