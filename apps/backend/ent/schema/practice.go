package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
)

type Practice struct {
	ent.Schema
}

func (Practice) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (Practice) Fields() []ent.Field {
	return []ent.Field{
		field.String("name").NotEmpty(),
		field.String("address").Optional().Nillable(),
		field.String("logo_url").Optional().Nillable(),
	}
}

func (Practice) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("doctors", Doctor.Type),
	}
}
