package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type Patient struct {
	ent.Schema
}

func (Patient) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (Patient) Fields() []ent.Field {
	return []ent.Field{
		field.String("display_name").NotEmpty(),
		field.Time("birth_date").Optional().Nillable(),

		field.Enum("status").
			Values("Active", "Inactive").
			Default("Active"),

		// optional patient-side identity fields if you need invite flows by email
		field.String("email").Optional().Nillable(),

		// optional until a patient registers (legacy/invited patients may not have credentials)
		field.String("password_hash").Optional().Nillable().Sensitive().Comment("bcrypt hash of the patient's password"),

		// optional code for "doctor code / patient code" flows
		field.String("patient_code").Optional().Nillable(),

		// for future: last activity, etc.
		field.Time("last_entry_at").Optional().Nillable().Default(nil).UpdateDefault(func() time.Time { return time.Now() }),
	}
}

func (Patient) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("email").Unique().StorageKey("uq_patient_email"),
		index.Fields("patient_code").Unique().StorageKey("uq_patient_code"),
		index.Fields("status"),
	}
}

func (Patient) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("doctor_links", DoctorPatientLink.Type),
		edge.To("consumed_pairing_codes", PairingCode.Type),
		edge.To("entries", Entry.Type),
		edge.To("analysis_jobs", AnalysisJob.Type),
		edge.To("entry_shares", EntryShare.Type),
	}
}
