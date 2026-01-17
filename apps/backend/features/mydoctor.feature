Feature: Patient doctor view

  Scenario: Patient sees assigned doctor and practice
    Given the API is running
    And I register a doctor with email "doc@example.com" password "SuperSecret1" displayName "Dr Good"
    And I create a practice:
      | name    | Harmony Clinic |
      | address | 12 West Street |
    And I create a pairing code
    And I register a patient with email "pat@example.com" password "SuperSecret1" displayName "Pat"
    When I redeem the pairing code
    And I call patient GET "/patient/mydoctor"
    Then the response status should be 200
    And the response JSON field "doctor.displayName" should be "Dr Good"
    And the response JSON field "doctor.myDoctorPractice.name" should be "Harmony Clinic"
    And the response JSON field "doctor.myDoctorPractice.address" should be "12 West Street"

  Scenario: Patient without approved link sees not found
    Given the API is running
    And I register a patient with email "solo@example.com" password "SuperSecret1" displayName "Solo"
    When I call patient GET "/patient/mydoctor"
    Then the response status should be 404
    And the response JSON field "error" should be "no doctor assigned"

  Scenario: Pending link does not count as assigned
    Given the API is running
    And I register a doctor with email "pendingdoc@example.com" password "SuperSecret1" displayName "Pending Doc"
    And I register a patient with email "pendingpat@example.com" password "SuperSecret1" displayName "Pending Pat"
    When I invite the patient by email:
      | patientEmail | pendingpat@example.com |
    And I call patient GET "/patient/mydoctor"
    Then the response status should be 404
    And the response JSON field "error" should be "no doctor assigned"
