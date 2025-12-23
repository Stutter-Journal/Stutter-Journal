package bddtest

import (
	"strings"

	"github.com/cucumber/godog"
)

func TableToMap(table *godog.Table) map[string]string {
	out := map[string]string{}
	if table == nil || len(table.Rows) == 0 {
		return out
	}

	start := 0
	if len(table.Rows[0].Cells) >= 2 {
		c0 := strings.ToLower(strings.TrimSpace(table.Rows[0].Cells[0].Value))
		c1 := strings.ToLower(strings.TrimSpace(table.Rows[0].Cells[1].Value))
		if (c0 == "field" || c0 == "key") && (c1 == "value" || c1 == "val") {
			start = 1
		}
	}

	for i := start; i < len(table.Rows); i++ {
		row := table.Rows[i]
		if len(row.Cells) < 2 {
			continue
		}
		k := strings.TrimSpace(row.Cells[0].Value)
		v := strings.TrimSpace(row.Cells[1].Value)
		out[k] = v
	}

	return out
}
