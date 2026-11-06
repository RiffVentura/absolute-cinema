FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Tools are baked into the image rather than installed from postCreateCommand,
# because Zed doesn't reliably run lifecycle scripts.
USER vscode

# Claude Code: native installer (the claude-code feature apt-installs a system Node that breaks the node feature)
RUN curl -fsSL https://claude.ai/install.sh | bash
# Bun: the frontend's package manager (bun.lock, angular.json cli.packageManager)
RUN curl -fsSL https://bun.sh/install | bash

USER root
RUN ln -s /home/vscode/.local/bin/claude /usr/local/bin/claude \
 && ln -s /home/vscode/.bun/bin/bun /usr/local/bin/bun \
 && ln -s /home/vscode/.bun/bin/bunx /usr/local/bin/bunx \
 # Pre-create volume mount points so named volumes inherit vscode ownership instead of root
 && mkdir -p /home/vscode/.claude /home/vscode/.m2 \
 && chown vscode:vscode /home/vscode/.claude /home/vscode/.m2

# Must equal the exact @playwright/test version in frontend/package.json, or the library
# looks for a browser build that isn't installed.
ARG PLAYWRIGHT_VERSION=1.64.1
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN bunx playwright@${PLAYWRIGHT_VERSION} install --with-deps chromium \
 && chmod -R a+rX /ms-playwright \
 && rm -rf /var/lib/apt/lists/* /root/.bun/install/cache
