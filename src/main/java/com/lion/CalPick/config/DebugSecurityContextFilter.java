package com.lion.CalPick.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class DebugSecurityContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DebugSecurityContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("*** DebugSecurityContextFilter: Before chain. SecurityContextHolder Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
        logger.info("*** DebugSecurityContextFilter: After chain. SecurityContextHolder Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
    }
}
