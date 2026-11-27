package com.lb_calc_web.security.jwt;

import java.util.Objects;

/** Пара JWT-токенов, возвращаемая прикладным сервисом аутентификации. */
public record AuthTokens(String accessToken, String refreshToken) {

    public AuthTokens {
        Objects.requireNonNull(accessToken, "Access token не должен быть null");
        Objects.requireNonNull(refreshToken, "Refresh token не должен быть null");
    }
}
