package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type Emotion struct {
	Name      string `json:"name"`
	Intensity int    `json:"intensity"` // 0..10
}

type Entry struct {
	ent.Schema
}

func (Entry) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (Entry) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("patient_id", uuid.UUID{}),

		field.Time("happened_at").
			Default(time.Now),

		field.String("situation").Optional().Nillable(),

		field.JSON("emotions", []Emotion{}).Optional(),
		field.JSON("triggers", []string{}).Optional(),
		field.JSON("techniques", []string{}).Optional(),

		field.Int("stutter_frequency").Optional().Nillable(), // 0..10

		field.String("notes").Optional().Nillable(),
		field.JSON("tags", []string{}).Optional(),
	}
}

func (Entry) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("patient_id", "happened_at"),
		index.Fields("happened_at"),
	}
}

func (Entry) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("patient", Patient.Type).
			Ref("entries").
			Field("patient_id").
			Unique().
			Required(),

		edge.To("shares", EntryShare.Type),
		edge.To("comments", Comment.Type),
		edge.To("analysis_jobs", AnalysisJob.Type),
	}
}
