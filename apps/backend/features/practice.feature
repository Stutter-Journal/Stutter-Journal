Feature: Practice onboarding

  Scenario: Create practice and assign doctor as owner
    Given the API is running
    And I register a doctor with email "owner@example.com" password "SuperSecret1" displayName "Owner"
    When I create a practice:
      | name    | Owner Practice |
      | address | 123 Clinic Rd  |
    Then the response status should be 201
    And the response JSON field "practice.name" should be "Owner Practice"
    And the current doctor is assigned to the created practice
    And the current doctor role is "Owner"
