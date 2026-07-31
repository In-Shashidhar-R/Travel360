package com.cts.util;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String[] PUBLIC_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/users/register",
            "/api/v1/users/login",
            "/api/v1/users/reset-password"
    };

    public static final long JWT_VALIDITY_MS = 1000L * 60 * 60;

    public static final int PENALTY_NEAR_DAYS = 5;
    public static final int PENALTY_MID_DAYS = 15;
    public static final double PENALTY_RATE_NEAR = 0.60;
    public static final double PENALTY_RATE_MID = 0.35;
    public static final double PENALTY_RATE_FAR = 0.20;

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 1000;
}
