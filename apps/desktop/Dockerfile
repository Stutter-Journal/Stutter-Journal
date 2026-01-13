# syntax=docker/dockerfile:1

FROM node:25-bullseye-slim AS build
WORKDIR /workspace

# Install pnpm explicitly (avoid relying on corepack availability)
RUN npm i -g pnpm

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml nx.json tsconfig.base.json ./
COPY . .

RUN pnpm install --frozen-lockfile

# Build targets (keep as you have for now)
RUN pnpm nx build bff && pnpm nx build portal

FROM node:25-alpine
WORKDIR /app
ENV NODE_ENV=production

COPY --from=build /workspace/node_modules ./node_modules
COPY --from=build /workspace/dist ./dist
COPY --from=build /workspace/package.json ./package.json

RUN printf '%s\n' \
  '#!/bin/sh' \
  'set -eu' \
  'if [ -f /app/dist/apps/portal/server/server.mjs ]; then exec node /app/dist/apps/portal/server/server.mjs; fi' \
  'if [ -f /app/dist/apps/portal/server/server.js ]; then exec node /app/dist/apps/portal/server/server.js; fi' \
  'echo "Portal server entry not found under dist/apps/portal/server" >&2' \
  'ls -la /app/dist/apps/portal/server || true' \
  'exit 1' \
  > /usr/local/bin/run-portal && chmod +x /usr/local/bin/run-portal

RUN printf '%s\n' \
  '#!/bin/sh' \
  'set -eu' \
  'if [ -f /app/dist/apps/api/main.js ]; then exec node /app/dist/apps/api/main.js; fi' \
  'if [ -f /app/dist/apps/bff/main.js ]; then exec node /app/dist/apps/bff/main.js; fi' \
  'echo "BFF entry not found under dist/apps/api or dist/apps/bff" >&2' \
  'ls -la /app/dist/apps || true' \
  'exit 1' \
  > /usr/local/bin/run-bff && chmod +x /usr/local/bin/run-bff

EXPOSE 4000
EXPOSE 3000

CMD ["/usr/local/bin/run-portal"]