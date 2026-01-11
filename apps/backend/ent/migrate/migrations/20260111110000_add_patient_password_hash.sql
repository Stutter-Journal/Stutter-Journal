-- Add password_hash to patients for credential storage
ALTER TABLE "public"."patients" ADD COLUMN "password_hash" character varying;
