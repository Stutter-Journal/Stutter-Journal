-- Create "pairing_codes" table
CREATE TABLE "public"."pairing_codes" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "code" character varying(6) NOT NULL,
  "doctor_id" uuid NOT NULL,
  "expires_at" timestamptz NOT NULL,
  "consumed_at" timestamptz NULL,
  "consumed_by_patient_id" uuid NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "pairing_codes_doctors_pairing_codes" FOREIGN KEY ("doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "pairing_codes_patients_consumed_pairing_codes" FOREIGN KEY ("consumed_by_patient_id") REFERENCES "public"."patients" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);
-- Create index "pairingcode_code" to table: "pairing_codes"
CREATE INDEX "pairingcode_code" ON "public"."pairing_codes" ("code");
-- Create index "pairingcode_doctor_id" to table: "pairing_codes"
CREATE INDEX "pairingcode_doctor_id" ON "public"."pairing_codes" ("doctor_id");
-- Create index "pairingcode_expires_at" to table: "pairing_codes"
CREATE INDEX "pairingcode_expires_at" ON "public"."pairing_codes" ("expires_at");
