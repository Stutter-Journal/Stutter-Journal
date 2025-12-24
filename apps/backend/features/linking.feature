Feature: Patient linking workflow

  Scenario: Doctor invites a patient, sees pending, and approves
    Given the API is running
    And I register a doctor with email "linkdoc@example.com" password "SuperSecret1" displayName "Link Doc"
    When I invite a patient:
      | displayName  | Alice Patient |
      | patientEmail | alice@example.com |
    Then the response status should be 201
    And the response JSON field "link.status" should be "Pending"
    And the response JSON field "patient.displayName" should be "Alice Patient"
    When I call GET "/patients"
    Then the response status should be 200
    And the response JSON field "pendingLinks.0.status" should be "Pending"
    When I approve the pending link
    Then the response status should be 200
    And the response JSON field "link.status" should be "Approved"
    When I call GET "/patients"
    Then the response status should be 200
    And the response JSON field "patients.0.displayName" should be "Alice Patient"
    And the response JSON field "pendingLinks" should be "[]"
