-- Add password_hash to doctors for credential storage
ALTER TABLE "public"."doctors" ADD COLUMN "password_hash" character varying NOT NULL DEFAULT 'legacy-password';
ALTER TABLE "public"."doctors" ALTER COLUMN "password_hash" DROP DEFAULT;
