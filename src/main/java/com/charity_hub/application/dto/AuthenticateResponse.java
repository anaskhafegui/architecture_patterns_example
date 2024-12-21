package com.charity_hub.application;

public record AuthenticateResponse(String accessToken, String refreshToken) {
}