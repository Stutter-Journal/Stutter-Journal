package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type PairingCode struct {
	ent.Schema
}

func (PairingCode) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (PairingCode) Fields() []ent.Field {
	return []ent.Field{
		field.String("code").NotEmpty().MaxLen(6),

		field.UUID("doctor_id", uuid.UUID{}),

		field.Time("expires_at"),

		field.Time("consumed_at").Optional().Nillable(),
		field.UUID("consumed_by_patient_id", uuid.UUID{}).Optional().Nillable(),
	}
}

func (PairingCode) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("code"),
		index.Fields("doctor_id"),
		index.Fields("expires_at"),
	}
}

func (PairingCode) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("doctor", Doctor.Type).
			Ref("pairing_codes").
			Field("doctor_id").
			Unique().
			Required(),

		edge.From("consumed_by_patient", Patient.Type).
			Ref("consumed_pairing_codes").
			Field("consumed_by_patient_id").
			Unique(),
	}
}
