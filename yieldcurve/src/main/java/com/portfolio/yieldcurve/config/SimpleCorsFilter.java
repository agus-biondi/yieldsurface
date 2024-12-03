package com.portfolio.yieldcurve.config;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SimpleCorsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCorsFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        logger.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());

        res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token");
        res.addHeader("Access-Control-Expose-Headers", "xsrf-token");

        // Log the setting of CORS headers
        logger.info("CORS headers added for request: {}", req.getRequestURI());

        // Handle OPTIONS request directly
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            logger.info("Preflight (OPTIONS) request handled");
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Proceed with the next filter in the chain
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Optional initialization
    }

    @Override
    public void destroy() {
        // Optional cleanup
    }
}


