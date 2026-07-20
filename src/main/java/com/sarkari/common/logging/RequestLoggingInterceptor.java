package com.sarkari.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "requestStartNs";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());
        log.info("HTTP request start method={} path={} query={} remoteIp={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startNs = request.getAttribute(START_TIME_ATTR);
        long durationMs = -1L;
        if (startNs instanceof Long) {
            durationMs = (System.nanoTime() - (Long) startNs) / 1_000_000;
        }

        if (ex == null) {
            log.info("HTTP request end method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);
            return;
        }

        log.error("HTTP request failed method={} path={} status={} durationMs={} errorType={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
