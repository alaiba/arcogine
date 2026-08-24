package com.arcogine.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final int MAX_BODY_BYTES = 1024 * 1024;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String origin = System.getenv("CORS_ALLOWED_ORIGIN");
        var mapping = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");

        if (origin != null && !origin.isBlank()) {
            mapping.allowedOrigins(origin);
        } else {
            mapping.allowedOriginPatterns("*");
        }
    }

    @Bean
    public FilterRegistrationBean<MaxBodySizeFilter> maxBodySizeFilter() {
        FilterRegistrationBean<MaxBodySizeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new MaxBodySizeFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        return bean;
    }

    static final class MaxBodySizeFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            long contentLength = request.getContentLengthLong();
            if (contentLength > MAX_BODY_BYTES) {
                response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request body too large");
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
