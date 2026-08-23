# syntax=docker/dockerfile:1.7
# Runtime-only image: packages the static site already built by
# `./arcogine build` (Vite produces dist/web/). This Dockerfile does not
# run npm/Vite — build context is dist/web/.
#
# Alpine + nginx installed from the Alpine repo. The official `nginx:*-alpine`
# image pins nginx from nginx.org's repo, so `apk upgrade` cannot pull
# Alpine's security-patched nginx build (e.g. CVE-2026-9256). Installing
# nginx via apk keeps it upgradeable with the rest of the OS.
FROM alpine:3.23

RUN apk add --no-cache nginx ca-certificates \
    && apk upgrade --no-cache \
    && mkdir -p /run/nginx /usr/share/nginx/html

COPY . /usr/share/nginx/html

# Alpine's nginx.conf includes /etc/nginx/http.d/*.conf; this replaces the
# packaged default server block.
COPY <<'EOF' /etc/nginx/http.d/default.conf
server {
    listen 5173;
    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self'; img-src 'self' data:; font-src 'self';" always;

    location = /health {
        add_header Content-Type text/plain;
        return 200 "ok";
    }

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://api:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_buffering off;
        proxy_cache off;
    }
}
EOF

EXPOSE 5173

CMD ["nginx", "-g", "daemon off;"]
