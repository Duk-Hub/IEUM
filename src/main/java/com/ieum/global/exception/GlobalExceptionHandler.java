package com.ieum.global.exception;

import com.ieum.global.response.ApiResponse;
import com.ieum.global.response.ErrorResponse;
import com.ieum.global.response.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e){
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException occurred. code={} message={}", errorCode, e.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(ErrorResponse.of(errorCode.name(), errorCode.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        List<ValidationError> fieldErrors = extractFieldErrors(e);
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(ErrorResponse.of(errorCode.name(), errorCode.getMessage(),fieldErrors)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e){
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("Unhandled Exception occurred",e);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(ErrorResponse.of(errorCode.name(), errorCode.getMessage())));
    }

    private List<ValidationError> extractFieldErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ValidationError.of(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();
    }
}
