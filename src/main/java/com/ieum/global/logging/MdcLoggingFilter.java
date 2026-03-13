package com.ieum.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        setMdc(request);

        log.info("Http request started");

        try {

            filterChain.doFilter(request, response);

        } finally {

            long elapsedMs = System.currentTimeMillis() - startTime;

            log.info("Http request completed status={} elapsed={}ms", response.getStatus(), elapsedMs);

            MDC.clear();
        }
    }

    private void setMdc(HttpServletRequest request) {
        MDC.put(MdcConst.TRACE_ID, generateTraceId());
        MDC.put(MdcConst.METHOD, request.getMethod());
        MDC.put(MdcConst.URI, request.getRequestURI());
        MDC.put(MdcConst.CLIENT_IP, extractClientIp(request));
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.startsWith("/favicon.ico") || uri.startsWith("/actuator");
    }
}
