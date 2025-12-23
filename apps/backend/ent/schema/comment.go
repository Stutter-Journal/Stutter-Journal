package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type Comment struct {
	ent.Schema
}

func (Comment) Mixin() []ent.Mixin {
	// created_at is enough for comment; updated_at optional
	return []ent.Mixin{
		UUIDMixin{},
	}
}

func (Comment) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("entry_id", uuid.UUID{}),
		field.UUID("author_doctor_id", uuid.UUID{}),

		field.String("body").NotEmpty(),

		field.Time("created_at").Default(time.Now).Immutable(),
	}
}

func (Comment) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("entry_id", "created_at"),
		index.Fields("author_doctor_id"),
	}
}

func (Comment) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("entry", Entry.Type).
			Ref("comments").
			Field("entry_id").
			Unique().
			Required(),

		edge.From("author", Doctor.Type).
			Ref("comments").
			Field("author_doctor_id").
			Unique().
			Required(),
	}
}
