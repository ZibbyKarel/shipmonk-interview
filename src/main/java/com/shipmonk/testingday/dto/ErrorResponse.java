package com.shipmonk.testingday.dto;

public record ErrorResponse(ErrorDetail error) {

    public ErrorResponse(int code, String info) {
        this(new ErrorDetail(code, info));
    }

    public boolean isSuccess() { return false; }

    public record ErrorDetail(int code, String info) {}
}
