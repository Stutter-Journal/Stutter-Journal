package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type DoctorPatientLink struct {
	ent.Schema
}

func (DoctorPatientLink) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (DoctorPatientLink) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("doctor_id", uuid.UUID{}),
		field.UUID("patient_id", uuid.UUID{}),

		field.Enum("status").
			Values("Pending", "Approved", "Denied", "Revoked").
			Default("Pending"),

		field.Time("requested_at").
			Default(time.Now),

		field.Time("approved_at").Optional().Nillable(),

		// audit: which doctor approved (usually same as doctor_id, but keep explicit)
		field.UUID("approved_by_doctor_id", uuid.UUID{}).Optional().Nillable(),
	}
}

func (DoctorPatientLink) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("doctor_id", "patient_id").Unique(),
		index.Fields("status"),
		index.Fields("requested_at"),
	}
}

func (DoctorPatientLink) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("doctor", Doctor.Type).
			Ref("patient_links").
			Field("doctor_id").
			Unique().
			Required(),

		edge.From("patient", Patient.Type).
			Ref("doctor_links").
			Field("patient_id").
			Unique().
			Required(),

		edge.From("approved_by", Doctor.Type).
			Ref("approved_patient_links").
			Field("approved_by_doctor_id").
			Unique().
			Comment("Doctor who approved the link (audit)."),
	}
}
