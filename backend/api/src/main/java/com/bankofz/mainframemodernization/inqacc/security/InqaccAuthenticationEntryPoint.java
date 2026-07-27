package com.bankofz.mainframemodernization.inqacc.security;

import com.bankofz.mainframemodernization.inqacc.domain.ErrorResponse;
import com.bankofz.mainframemodernization.inqacc.logging.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class InqaccAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public InqaccAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(new ErrorResponse.ErrorBody(
                "ERR-002",
                "Unauthorized",
                Instant.now().toString(),
                (String) request.getAttribute(CorrelationIdFilter.CORRELATION_ATTRIBUTE),
                null
        ));

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
