package com.malitool.authentication.config;

import com.malitool.authentication.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                BAD_REQUEST,
                List.of(e.getMessage())
        );
        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
}
