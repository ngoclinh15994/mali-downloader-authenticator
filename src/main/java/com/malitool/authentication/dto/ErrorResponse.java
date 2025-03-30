package com.malitool.authentication.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ErrorResponse {
    HttpStatus httpStatus;
    List<String> messages;

    public ErrorResponse(HttpStatus httpStatus, List<String> messages) {
        this.httpStatus = httpStatus;
        this.messages = messages;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}