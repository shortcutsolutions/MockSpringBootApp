package com.reliaquest.api.filter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * @author Hussain Rana
 */
@Slf4j
@Component
public class RequestLogger extends CommonsRequestLoggingFilter {


    public RequestLogger() {
        // Configure logging behavior
        setIncludeQueryString(true);
        setIncludePayload(true);
        setMaxPayloadLength(10000);
        setIncludeHeaders(false);
        setIncludeClientInfo(true);
        setBeforeMessagePrefix("");
        setAfterMessagePrefix("");
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        request.setAttribute("apiRequestStartTime", System.currentTimeMillis());
        log.debug("Request started.  message={}, HTTPMethod={}", message, request.getMethod());
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        long startTime = (long)request.getAttribute("apiRequestStartTime");
        long endTime = System.currentTimeMillis();
        log.debug("Request Completed. message={}, HTTPMethod={}. TimeTakenToComplete={} ms", message, request.getMethod(), (endTime-startTime));
    }
}

