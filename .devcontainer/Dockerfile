FROM mcr.microsoft.com/devcontainers/rust:1-bookworm

ARG NODE_MAJOR=24
ARG TRIVY_VERSION=0.69.3
ARG GITLEAKS_VERSION=8.30.1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
        gnupg \
        pkg-config \
        libssl-dev \
        fonts-freefont-ttf \
        fonts-ipafont-gothic \
        fonts-liberation \
        fonts-noto-color-emoji \
        fonts-tlwg-loma-otf \
        fonts-wqy-zenhei \
        libasound2 \
        libatk-bridge2.0-0 \
        libatk1.0-0 \
        libatspi2.0-0 \
        libcairo2 \
        libcups2 \
        libdbus-1-3 \
        libdrm2 \
        libfontconfig1 \
        libfreetype6 \
        libgbm1 \
        libglib2.0-0 \
        libnss3 \
        libnspr4 \
        libpango-1.0-0 \
        libx11-xcb1 \
        libx11-6 \
        libxcomposite1 \
        libxdamage1 \
        libxext6 \
        libxfixes3 \
        libxkbcommon0 \
        libxrandr2 \
        libxshmfence1 \
        libxcb1 \
        xvfb \
    && mkdir -p /usr/local/bin \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
        | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" \
        > /etc/apt/sources.list.d/nodesource.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends nodejs \
    && TRIVY_ARCH="$(dpkg --print-architecture | sed 's/amd64/x86_64/;s/arm64/aarch64/')" \
    && case "$TRIVY_ARCH" in \
      x86_64) TRIVY_ASSET="Linux-64bit"; GITLEAKS_ASSET="linux_x64" ;; \
      aarch64) TRIVY_ASSET="Linux-ARM64"; GITLEAKS_ASSET="linux_arm64" ;; \
      *) echo "Unsupported architecture: $TRIVY_ARCH"; exit 1 ;; \
      esac \
    && curl -fsSL "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_${TRIVY_ASSET}.tar.gz" \
        -o /tmp/trivy.tar.gz \
    && tar -xzf /tmp/trivy.tar.gz -C /tmp trivy \
    && install -m 0755 /tmp/trivy /usr/local/bin/trivy \
    && curl -fsSL "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_${GITLEAKS_ASSET}.tar.gz" \
        -o /tmp/gitleaks.tar.gz \
    && tar -xzf /tmp/gitleaks.tar.gz -C /tmp gitleaks \
    && install -m 0755 /tmp/gitleaks /usr/local/bin/gitleaks \
    && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && rm -f /tmp/trivy.tar.gz /tmp/gitleaks.tar.gz

RUN rustup component add rustfmt clippy llvm-tools \
    && cargo install cargo-watch \
    && cargo install --locked cargo-nextest \
    && cargo install --locked cargo-llvm-cov \
    && cargo install --locked cargo-audit

USER vscode
