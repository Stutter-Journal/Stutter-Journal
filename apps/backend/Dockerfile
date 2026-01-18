# syntax=docker/dockerfile:1

FROM golang:1.25.6-alpine AS build

WORKDIR /src

RUN apk add --no-cache git ca-certificates

COPY go.mod go.sum ./
RUN go mod download

COPY . .

# Build a static-ish binary (alpine/musl). Do not hardcode GOARCH so this works on
# both amd64 and arm64 Docker Desktop nodes.
RUN CGO_ENABLED=0 \
  go build -trimpath -ldflags="-s -w" -o /out/backend ./cmd/api

FROM alpine:latest

RUN apk add --no-cache ca-certificates && \
  adduser -D -s /sbin/nologin app && \
  mkdir -p /home/app /app && \
  chown -R app:app /home/app /app

WORKDIR /app

COPY --from=build /out/backend /usr/local/bin/backend

USER app

ENV HOME=/home/app
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/backend"]
