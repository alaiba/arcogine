package com.arcogine.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
            long declaredLength = request.getContentLengthLong();
            if (declaredLength > MAX_BODY_BYTES) {
                rejectOversized(response);
                return;
            }

            if (declaredLength < 0) {
                // A request with no Content-Length -- chunked transfer encoding being the
                // usual case -- reports -1 here, so the declared-length check above cannot
                // bound it. Read the body, stopping one byte past the limit, and replay the
                // buffered bytes downstream so the handler still sees an ordinary request.
                byte[] body = readAtMost(request.getInputStream(), MAX_BODY_BYTES + 1L);
                if (body.length > MAX_BODY_BYTES) {
                    rejectOversized(response);
                    return;
                }
                filterChain.doFilter(new BufferedBodyRequest(request, body), response);
                return;
            }

            filterChain.doFilter(request, response);
        }

        private static void rejectOversized(HttpServletResponse response) throws IOException {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request body too large");
        }

        private static byte[] readAtMost(InputStream source, long limit) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            long remaining = limit;
            while (remaining > 0) {
                int read = source.read(chunk, 0, (int) Math.min(chunk.length, remaining));
                if (read < 0) {
                    break;
                }
                buffer.write(chunk, 0, read);
                remaining -= read;
            }
            return buffer.toByteArray();
        }
    }

    /** Replays an already-buffered request body so the body can be read twice. */
    private static final class BufferedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        BufferedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream source = new ByteArrayInputStream(body);
            return new ServletInputStream() {

                @Override
                public int read() {
                    return source.read();
                }

                @Override
                public int read(byte[] target, int off, int len) {
                    return source.read(target, off, len);
                }

                @Override
                public boolean isFinished() {
                    return source.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException("Asynchronous reads are not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), resolveCharset()));
        }

        private Charset resolveCharset() {
            String encoding = getCharacterEncoding();
            if (encoding == null || encoding.isBlank()) {
                return StandardCharsets.UTF_8;
            }
            return Charset.forName(encoding);
        }
    }
}
