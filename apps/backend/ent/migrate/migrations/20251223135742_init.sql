-- Create "practices" table
CREATE TABLE "public"."practices" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "name" character varying NOT NULL,
  "address" character varying NULL,
  "logo_url" character varying NULL,
  PRIMARY KEY ("id")
);
-- Create "doctors" table
CREATE TABLE "public"."doctors" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "email" character varying NOT NULL,
  "display_name" character varying NOT NULL,
  "role" character varying NOT NULL DEFAULT 'Owner',
  "practice_id" uuid NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "doctors_practices_doctors" FOREIGN KEY ("practice_id") REFERENCES "public"."practices" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);
-- Create index "doctor_email" to table: "doctors"
CREATE UNIQUE INDEX "doctor_email" ON "public"."doctors" ("email");
-- Create index "doctor_practice_id" to table: "doctors"
CREATE INDEX "doctor_practice_id" ON "public"."doctors" ("practice_id");
-- Create "patients" table
CREATE TABLE "public"."patients" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "display_name" character varying NOT NULL,
  "birth_date" timestamptz NULL,
  "status" character varying NOT NULL DEFAULT 'Active',
  "email" character varying NULL,
  "patient_code" character varying NULL,
  "last_entry_at" timestamptz NULL,
  PRIMARY KEY ("id")
);
-- Create index "patient_status" to table: "patients"
CREATE INDEX "patient_status" ON "public"."patients" ("status");
-- Create index "uq_patient_code" to table: "patients"
CREATE UNIQUE INDEX "uq_patient_code" ON "public"."patients" ("patient_code");
-- Create index "uq_patient_email" to table: "patients"
CREATE UNIQUE INDEX "uq_patient_email" ON "public"."patients" ("email");
-- Create "entries" table
CREATE TABLE "public"."entries" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "happened_at" timestamptz NOT NULL,
  "situation" character varying NULL,
  "emotions" jsonb NULL,
  "triggers" jsonb NULL,
  "techniques" jsonb NULL,
  "stutter_frequency" bigint NULL,
  "notes" character varying NULL,
  "tags" jsonb NULL,
  "patient_id" uuid NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "entries_patients_entries" FOREIGN KEY ("patient_id") REFERENCES "public"."patients" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION
);
-- Create index "entry_happened_at" to table: "entries"
CREATE INDEX "entry_happened_at" ON "public"."entries" ("happened_at");
-- Create index "entry_patient_id_happened_at" to table: "entries"
CREATE INDEX "entry_patient_id_happened_at" ON "public"."entries" ("patient_id", "happened_at");
-- Create "analysis_jobs" table
CREATE TABLE "public"."analysis_jobs" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "object_key" character varying NOT NULL,
  "kind" character varying NOT NULL,
  "status" character varying NOT NULL DEFAULT 'Queued',
  "progress" bigint NULL,
  "result" jsonb NULL,
  "metrics" jsonb NULL,
  "error_message" character varying NULL,
  "started_at" timestamptz NULL,
  "finished_at" timestamptz NULL,
  "created_by_doctor_id" uuid NOT NULL,
  "entry_id" uuid NULL,
  "patient_id" uuid NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "analysis_jobs_doctors_created_analysis_jobs" FOREIGN KEY ("created_by_doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "analysis_jobs_entries_analysis_jobs" FOREIGN KEY ("entry_id") REFERENCES "public"."entries" ("id") ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT "analysis_jobs_patients_analysis_jobs" FOREIGN KEY ("patient_id") REFERENCES "public"."patients" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION
);
-- Create index "analysisjob_created_by_doctor_id" to table: "analysis_jobs"
CREATE INDEX "analysisjob_created_by_doctor_id" ON "public"."analysis_jobs" ("created_by_doctor_id");
-- Create index "analysisjob_patient_id_created_at" to table: "analysis_jobs"
CREATE INDEX "analysisjob_patient_id_created_at" ON "public"."analysis_jobs" ("patient_id", "created_at");
-- Create index "analysisjob_status" to table: "analysis_jobs"
CREATE INDEX "analysisjob_status" ON "public"."analysis_jobs" ("status");
-- Create "comments" table
CREATE TABLE "public"."comments" (
  "id" uuid NOT NULL,
  "body" character varying NOT NULL,
  "created_at" timestamptz NOT NULL,
  "author_doctor_id" uuid NOT NULL,
  "entry_id" uuid NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "comments_doctors_comments" FOREIGN KEY ("author_doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "comments_entries_comments" FOREIGN KEY ("entry_id") REFERENCES "public"."entries" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION
);
-- Create index "comment_author_doctor_id" to table: "comments"
CREATE INDEX "comment_author_doctor_id" ON "public"."comments" ("author_doctor_id");
-- Create index "comment_entry_id_created_at" to table: "comments"
CREATE INDEX "comment_entry_id_created_at" ON "public"."comments" ("entry_id", "created_at");
-- Create "doctor_patient_links" table
CREATE TABLE "public"."doctor_patient_links" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "status" character varying NOT NULL DEFAULT 'Pending',
  "requested_at" timestamptz NOT NULL,
  "approved_at" timestamptz NULL,
  "doctor_id" uuid NOT NULL,
  "approved_by_doctor_id" uuid NULL,
  "patient_id" uuid NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "doctor_patient_links_doctors_approved_patient_links" FOREIGN KEY ("approved_by_doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT "doctor_patient_links_doctors_patient_links" FOREIGN KEY ("doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "doctor_patient_links_patients_doctor_links" FOREIGN KEY ("patient_id") REFERENCES "public"."patients" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION
);
-- Create index "doctorpatientlink_doctor_id_patient_id" to table: "doctor_patient_links"
CREATE UNIQUE INDEX "doctorpatientlink_doctor_id_patient_id" ON "public"."doctor_patient_links" ("doctor_id", "patient_id");
-- Create index "doctorpatientlink_requested_at" to table: "doctor_patient_links"
CREATE INDEX "doctorpatientlink_requested_at" ON "public"."doctor_patient_links" ("requested_at");
-- Create index "doctorpatientlink_status" to table: "doctor_patient_links"
CREATE INDEX "doctorpatientlink_status" ON "public"."doctor_patient_links" ("status");
-- Create "entry_shares" table
CREATE TABLE "public"."entry_shares" (
  "id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "shared_at" timestamptz NOT NULL,
  "revoked_at" timestamptz NULL,
  "shared_with_doctor_id" uuid NOT NULL,
  "entry_id" uuid NOT NULL,
  "shared_by_patient_id" uuid NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "entry_shares_doctors_entry_shares" FOREIGN KEY ("shared_with_doctor_id") REFERENCES "public"."doctors" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "entry_shares_entries_shares" FOREIGN KEY ("entry_id") REFERENCES "public"."entries" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "entry_shares_patients_entry_shares" FOREIGN KEY ("shared_by_patient_id") REFERENCES "public"."patients" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION
);
-- Create index "entryshare_entry_id_shared_with_doctor_id" to table: "entry_shares"
CREATE UNIQUE INDEX "entryshare_entry_id_shared_with_doctor_id" ON "public"."entry_shares" ("entry_id", "shared_with_doctor_id");
-- Create index "entryshare_shared_at" to table: "entry_shares"
CREATE INDEX "entryshare_shared_at" ON "public"."entry_shares" ("shared_at");
-- Create index "entryshare_shared_by_patient_id" to table: "entry_shares"
CREATE INDEX "entryshare_shared_by_patient_id" ON "public"."entry_shares" ("shared_by_patient_id");
-- Create index "entryshare_shared_with_doctor_id" to table: "entry_shares"
CREATE INDEX "entryshare_shared_with_doctor_id" ON "public"."entry_shares" ("shared_with_doctor_id");
