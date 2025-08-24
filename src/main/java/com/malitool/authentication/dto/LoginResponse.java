package com.malitool.authentication.dto;

public class LoginResponse {
    private String email;
    private String token;
    private SubscriptionDTO subscription;

    public LoginResponse(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public LoginResponse(String email, String token, SubscriptionDTO subscription) {
        this.email = email;
        this.token = token;
        this.subscription = subscription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SubscriptionDTO getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionDTO subscription) {
        this.subscription = subscription;
    }
}