package server

import (
	"net/http"
	"sort"
	"time"

	"backend/ent/entry"

	"github.com/charmbracelet/log"
	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type analyticsResponse struct {
	RangeDays     int                    `json:"rangeDays"`
	Distributions analyticsDistributions `json:"distributions"`
	Trend         []trendPoint           `json:"trend"`
}

type analyticsDistributions struct {
	Emotions   map[string]int `json:"emotions"`
	Triggers   map[string]int `json:"triggers"`
	Techniques map[string]int `json:"techniques"`
}

type trendPoint struct {
	Date             string  `json:"date"`
	AvgStutterRating float64 `json:"avgStutterFrequency"`
	Count            int     `json:"count"`
}

func (s *Server) analyticsHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	patientIDParam := chi.URLParam(r, "id")
	patientID, err := uuid.Parse(patientIDParam)
	if err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid patient id")
		return
	}

	if ok := s.hasApprovedLink(r.Context(), doc.ID, patientID); !ok {
		s.writeError(w, http.StatusForbidden, "no approved link for patient")
		return
	}

	rangeDays := parseRangeDays(r.URL.Query().Get("range"))
	to := time.Now().UTC()
	from := to.AddDate(0, 0, -rangeDays)

	entries, err := s.Db.Ent().Entry.Query().
		Where(
			entry.PatientIDEQ(patientID),
			entry.HappenedAtGTE(from),
			entry.HappenedAtLTE(to),
		).
		All(r.Context())
	if err != nil {
		log.Error("failed to fetch entries for analytics", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not compute analytics")
		return
	}

	resp := analyticsResponse{
		RangeDays: rangeDays,
		Distributions: analyticsDistributions{
			Emotions:   map[string]int{},
			Triggers:   map[string]int{},
			Techniques: map[string]int{},
		},
		Trend: []trendPoint{},
	}

	trendMap := map[string][]int{}

	for _, e := range entries {
		dateKey := e.HappenedAt.UTC().Format("2006-01-02")

		for _, emo := range e.Emotions {
			key := emo.Name
			resp.Distributions.Emotions[key]++
		}
		for _, trig := range e.Triggers {
			resp.Distributions.Triggers[trig]++
		}
		for _, tech := range e.Techniques {
			resp.Distributions.Techniques[tech]++
		}

		if e.StutterFrequency != nil {
			trendMap[dateKey] = append(trendMap[dateKey], *e.StutterFrequency)
		}
	}

	dates := make([]string, 0, len(trendMap))
	for d := range trendMap {
		dates = append(dates, d)
	}
	sort.Strings(dates)

	for _, d := range dates {
		points := trendMap[d]
		if len(points) == 0 {
			continue
		}
		sum := 0
		for _, v := range points {
			sum += v
		}
		resp.Trend = append(resp.Trend, trendPoint{
			Date:             d,
			AvgStutterRating: float64(sum) / float64(len(points)),
			Count:            len(points),
		})
	}

	s.writeJSON(w, http.StatusOK, resp)
}

func parseRangeDays(raw string) int {
	switch raw {
	case "30":
		return 30
	case "90":
		return 90
	default:
		return 7
	}
}
