# LAN access (Android/iOS) without ports

Goal: access the cluster from other devices on your LAN using stable hostnames on the default port:

- http://api.eloquia.test
- http://app.eloquia.test

This assumes your LAN DNS maps both names to your Mac.

## 1) Deploy Ingress hosts for `eloquia.test`

Run the deploy with an override so Terraform provisions Ingress for the right hosts:

```bash
ELOQUIA_BASE_HOST=eloquia.test ./scripts/up.sh
```

This produces `.env.runtime` containing URLs under `*.eloquia.test`.

## 2) Bridge LAN port 80 to Docker Desktop's localhost ingress

Docker Desktop's Kubernetes ingress is typically reachable only from the Mac itself (localhost).
To make it reachable from other LAN devices on port 80, run a local reverse proxy.

### Caddy (recommended)

Install:

```bash
brew install caddy
```

Run (needs sudo to bind to port 80):

```bash
sudo caddy run --config ./infra/lan/Caddyfile
```

Now other devices on the LAN can hit:

- http://api.eloquia.test/health
- http://app.eloquia.test/

Stop with Ctrl+C.

## Notes

- This is HTTP-only by default (`auto_https off`). If you want HTTPS, you can add local certificates (e.g. mkcert) and install the CA on Android.
- The reverse proxy keeps the `Host` header, so ingress-nginx can route to the right service based on `api.*` vs `app.*`.
