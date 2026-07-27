package com.bankofz.mainframemodernization.inqacc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String PREFIX = "Bearer ";
    private static final String INQUIRER_TOKEN = "valid-inqacc-inquirer-token";
    private static final String LIMITED_TOKEN = "valid-inqacc-limited-token";

    private final InqaccAuthenticationEntryPoint authenticationEntryPoint;

    public BearerTokenAuthenticationFilter(InqaccAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String headerValue = request.getHeader(AUTHORIZATION);
        if (headerValue != null && !headerValue.isBlank()) {
            if (!headerValue.startsWith(PREFIX)) {
                authenticationEntryPoint.commence(request, response, new BadCredentialsException("Malformed bearer token"));
                return;
            }

            String token = headerValue.substring(PREFIX.length()).trim();
            if (INQUIRER_TOKEN.equals(token)) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "inqacc-user",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ACCOUNT_INQUIRER"))
                        )
                );
            } else if (LIMITED_TOKEN.equals(token)) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "limited-user",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
                        )
                );
            } else {
                authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid bearer token"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
