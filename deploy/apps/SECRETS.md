# Secrets (SOPS)

This repo stores secrets encrypted with SOPS. Each environment has a single
`secrets.enc.yaml` file that includes `postgres-auth` and `backend-secrets`.

Files:
- `deploy/apps/overlays/home/secrets.enc.yaml`
- `deploy/apps/overlays/staging/secrets.enc.yaml`
- `deploy/apps/overlays/production/secrets.enc.yaml`

## Setup

1) Generate an age key (if you don't have one already):

```bash
age-keygen -o age.agekey
```

2) Put the public key into `.sops.yaml` (replace `AGE_RECIPIENT_HERE`).

3) Replace `REPLACE_ME` values in each `secrets.enc.yaml`, then encrypt:

```bash
sops -e -i deploy/apps/overlays/<env>/secrets.enc.yaml
```

4) Create the Flux decryption secret (once per cluster):

```bash
kubectl -n flux-system create secret generic sops-age \
  --from-file=age.agekey=./age.agekey
```

Notes:
- `AUTH_COOKIE_SECRET` should be >= 32 bytes.
- Flux apps Kustomizations are already configured to use `sops-age`.
