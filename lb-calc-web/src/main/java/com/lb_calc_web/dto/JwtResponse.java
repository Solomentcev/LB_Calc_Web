package com.lb_calc_web.dto;

/** Пара JWT-токенов, возвращаемая после аутентификации или обновления сессии. */
/** DTO пары access и refresh JWT-токенов. */
public class JwtResponse {
    private final String accessToken;
    private final String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
