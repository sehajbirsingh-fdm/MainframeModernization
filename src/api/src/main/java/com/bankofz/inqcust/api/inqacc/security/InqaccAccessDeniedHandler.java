package com.bankofz.inqcust.api.inqacc.security;

import com.bankofz.inqcust.api.inqacc.domain.ErrorResponse;
import com.bankofz.inqcust.api.inqacc.logging.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class InqaccAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public InqaccAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(new ErrorResponse.ErrorBody(
                "ERR-003",
                "Forbidden",
                Instant.now().toString(),
                (String) request.getAttribute(CorrelationIdFilter.CORRELATION_ATTRIBUTE),
                null
        ));

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
