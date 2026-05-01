package com.shipmonk.testingday.exception;

import com.shipmonk.testingday.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    @ExceptionHandler(RatesNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RatesNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(FixerApiException.class)
    public ResponseEntity<ErrorResponse> handleFixerApi(FixerApiException ex) {
        int code = ex.getFixerCode() != 0 ? ex.getFixerCode() : 502;
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(code, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Invalid date format — expected YYYY-MM-DD, got: " + ex.getValue()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error"));
    }
}
