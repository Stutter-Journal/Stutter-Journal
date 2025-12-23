package bddtest

import (
	"encoding/json"
	"fmt"
	"strings"
)

func ExtractField(body []byte, path string) (string, error) {
	var data any
	if err := json.Unmarshal(body, &data); err != nil {
		return "", err
	}

	current := data
	for _, part := range strings.Split(path, ".") {
		m, ok := current.(map[string]any)
		if !ok {
			return "", fmt.Errorf("invalid path %q", path)
		}
		val, ok := m[part]
		if !ok {
			return "", fmt.Errorf("field %q missing", path)
		}
		current = val
	}

	return fmt.Sprint(current), nil
}
