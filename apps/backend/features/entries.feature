Feature: Patient entries timeline

  Scenario: Doctor with approved link views patient entries
    Given the API is running
    And I register a doctor with email "entriesdoc@example.com" password "SuperSecret1" displayName "Entries Doc"
    And patient "Pat One" has entries:
      | happenedAt                 | situation        | notes            |
      | 2025-01-01T10:00:00Z       | At school        | morning entry    |
      | 2025-01-02T12:00:00Z       | At clinic visit  | follow-up entry  |
    When I call GET "/patients/{patOneID}/entries?from=2025-01-01T00:00:00Z&to=2025-01-03T00:00:00Z"
    Then the response status should be 200
    And the response JSON field "entries.0.situation" should be "At school"
    And the response JSON field "entries.1.situation" should be "At clinic visit"
