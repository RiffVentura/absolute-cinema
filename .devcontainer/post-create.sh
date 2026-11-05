#!/usr/bin/env bash
# Optional convenience: installs frontend deps. Zed doesn't always run postCreateCommand,
# so nothing essential lives here (tools are baked into the image, see Dockerfile).
# Kept as a script because Zed doesn't run string commands through a shell.
set -euo pipefail

cd /workspaces/absolute-cinema/frontend
bun install
