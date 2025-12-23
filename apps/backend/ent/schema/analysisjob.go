package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type AnalysisJob struct {
	ent.Schema
}

func (AnalysisJob) Mixin() []ent.Mixin {
	return []ent.Mixin{
		UUIDMixin{},
		TimeMixin{},
	}
}

func (AnalysisJob) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("patient_id", uuid.UUID{}),
		field.UUID("created_by_doctor_id", uuid.UUID{}),

		// points to object storage key (S3/MinIO/etc)
		field.String("object_key").NotEmpty(),

		// e.g. "stutter_audio_v1"
		field.String("kind").NotEmpty(),

		field.Enum("status").
			Values("Queued", "Running", "Done", "Failed").
			Default("Queued"),

		field.Int("progress").Optional().Nillable(), // 0..100

		// store results small; large artifacts => object storage key instead
		field.JSON("result", map[string]any{}).Optional(),
		field.JSON("metrics", map[string]any{}).Optional(),

		field.String("error_message").Optional().Nillable(),

		field.Time("started_at").Optional().Nillable(),
		field.Time("finished_at").Optional().Nillable(),

		// optional correlation to an entry if you later create entries from analysis
		field.UUID("entry_id", uuid.UUID{}).Optional().Nillable(),
	}
}

func (AnalysisJob) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("patient_id", "created_at"),
		index.Fields("status"),
		index.Fields("created_by_doctor_id"),
	}
}

func (AnalysisJob) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("patient", Patient.Type).
			Ref("analysis_jobs").
			Field("patient_id").
			Unique().
			Required(),

		edge.From("created_by_doctor", Doctor.Type).
			Ref("created_analysis_jobs").
			Field("created_by_doctor_id").
			Unique().
			Required(),

		edge.From("entry", Entry.Type).
			Ref("analysis_jobs").
			Field("entry_id").
			Unique().
			Comment("Optional: job can be linked to an entry later."),
	}
}
