package bddtest

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/cookiejar"
	"time"
)

type Client struct {
	BaseURL string
	HTTP    *http.Client

	LastResp *http.Response
	LastBody []byte
}

func NewClient(baseURL string) *Client {
	jar, _ := cookiejar.New(nil)
	return &Client{
		BaseURL: baseURL,
		HTTP: &http.Client{
			Jar:     jar,
			Timeout: 10 * time.Second,
		},
	}
}

func (c *Client) Get(path string) error {
	req, err := http.NewRequest(http.MethodGet, c.BaseURL+path, nil)
	if err != nil {
		return err
	}
	return c.Do(req)
}

func (c *Client) PostJSON(path string, payload any) error {
	var body io.Reader
	if payload != nil {
		b, err := json.Marshal(payload)
		if err != nil {
			return err
		}
		body = bytes.NewReader(b)
	}

	req, err := http.NewRequest(http.MethodPost, c.BaseURL+path, body)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	return c.Do(req)
}

func (c *Client) Do(req *http.Request) error {
	resp, err := c.HTTP.Do(req)
	if err != nil {
		return err
	}
	defer func() {
		_ = resp.Body.Close()
	}()

	b, err := io.ReadAll(resp.Body)
	if err != nil {
		return err
	}

	c.LastResp = resp
	c.LastBody = b
	return nil
}

func (c *Client) RequireStatus(code int) error {
	if c.LastResp == nil {
		return fmt.Errorf("no response recorded")
	}
	if c.LastResp.StatusCode != code {
		return fmt.Errorf("expected status %d, got %d: %s", code, c.LastResp.StatusCode, string(c.LastBody))
	}
	return nil
}
