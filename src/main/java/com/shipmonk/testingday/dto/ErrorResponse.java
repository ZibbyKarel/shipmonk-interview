package com.shipmonk.testingday.dto;

public class ErrorResponse {

    private final boolean success = false;
    private final ErrorDetail error;

    public ErrorResponse(int code, String info) {
        this.error = new ErrorDetail(code, info);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorDetail getError() {
        return error;
    }

    public static class ErrorDetail {
        private final int code;
        private final String info;

        ErrorDetail(int code, String info) {
            this.code = code;
            this.info = info;
        }

        public int getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }
    }
}
