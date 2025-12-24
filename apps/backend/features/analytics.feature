Feature: Patient analytics

  Scenario: Doctor views analytics for a patient with entries
    Given the API is running
    And I register a doctor with email "analyticsdoc@example.com" password "SuperSecret1" displayName "Analytics Doc"
    And patient "Pat One" has analytics entries:
      | happenedAt | stutterFrequency | emotions        | triggers | techniques  |
      | today-2d   | 4                | calm,focused    | phone    | breathing   |
      | today-1d   | 6                | anxious,focused | meeting  | slow speech |
    When I call GET "/patients/{patOneID}/analytics?range=30"
    Then the response status should be 200
    And the response JSON field "distributions.emotions.focused" should be "2"
    And the response JSON field "distributions.triggers.phone" should be "1"
    And the response JSON field "distributions.techniques.breathing" should be "1"
    And the response JSON field "trend.0.avgStutterFrequency" should be "4"
    And the response JSON field "trend.1.avgStutterFrequency" should be "6"
