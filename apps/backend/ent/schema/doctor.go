package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type Doctor struct {
	ent.Schema
}

func (Doctor) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (Doctor) Fields() []ent.Field {
	return []ent.Field{
		field.String("email").NotEmpty(),
		field.String("display_name").NotEmpty(),
		field.String("password_hash").NotEmpty().Sensitive().Comment("bcrypt hash of the doctor's password"),

		field.Enum("role").Values("Owner", "Staff").Default("Owner"),

		field.UUID("practice_id", uuid.UUID{}).Optional().Nillable(),
	}
}

func (Doctor) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("email").Unique(),
		index.Fields("practice_id"),
	}
}

func (Doctor) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("practice", Practice.Type).
			Ref("doctors").
			Field("practice_id").
			Unique().
			// still optional because practice_id is optional
			// (Ent infers optionality from field)
			Comment("Doctor belongs to a practice (optional until onboarding)."),

		edge.To("patient_links", DoctorPatientLink.Type),
		edge.To("pairing_codes", PairingCode.Type),
		edge.To("approved_patient_links", DoctorPatientLink.Type), // Add this line
		edge.To("entry_shares", EntryShare.Type),
		edge.To("comments", Comment.Type),
		edge.To("created_analysis_jobs", AnalysisJob.Type),
	}
}
