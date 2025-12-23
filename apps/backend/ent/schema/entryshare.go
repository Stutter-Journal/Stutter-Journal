package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type EntryShare struct {
	ent.Schema
}

func (EntryShare) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (EntryShare) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("entry_id", uuid.UUID{}),
		field.UUID("shared_by_patient_id", uuid.UUID{}),
		field.UUID("shared_with_doctor_id", uuid.UUID{}),

		field.Time("shared_at").Default(time.Now),
		field.Time("revoked_at").Optional().Nillable(),
	}
}

func (EntryShare) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("entry_id", "shared_with_doctor_id").Unique(),
		index.Fields("shared_with_doctor_id"),
		index.Fields("shared_by_patient_id"),
		index.Fields("shared_at"),
	}
}

func (EntryShare) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("entry", Entry.Type).
			Ref("shares").
			Field("entry_id").
			Unique().
			Required(),

		edge.From("shared_by_patient", Patient.Type).
			Ref("entry_shares").
			Field("shared_by_patient_id").
			Unique().
			Required(),

		edge.From("shared_with_doctor", Doctor.Type).
			Ref("entry_shares").
			Field("shared_with_doctor_id").
			Unique().
			Required(),
	}
}
