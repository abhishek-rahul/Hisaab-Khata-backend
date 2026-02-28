package com.hisaab_khata.hisaab_khata.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 403 in same ApiResponse format as GlobalExceptionHandler,
 * when access is denied at filter level (e.g. SecurityConfig hasRole).
 */
@Component
public class ApiResponseAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error("FORBIDDEN", "ACCESS_DENIED",
                accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access denied");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
