Feature: Doctor authentication with session cookies

  Background:
    Given the API is running

  Scenario: Complete authentication lifecycle
    # Register
    When I register a doctor:
      | email       | authdoc@example.com |
      | password    | SuperSecret1        |
      | displayName | Auth Doc            |
    Then the response status should be 201
    And the response should match:
      | doctor.email | authdoc@example.com |
      | doctor.displayName  | Auth Doc            |

    # Login
    When I log in with:
      | email    | authdoc@example.com |
      | password | SuperSecret1        |
    Then the response status should be 200

    # Verify authenticated access
    When I call GET "/doctor/me"
    Then the response status should be 200
    And the response field "doctor.email" should be "authdoc@example.com"

    # Logout
    When I log out
    Then the response status should be 200

    # Verify session invalidated
    When I call GET "/doctor/me"
    Then the response should be unauthorized

  Scenario: Patient authentication lifecycle
    # Register
    When I register a patient:
      | email       | authpatient@example.com |
      | password    | SuperSecret1            |
      | displayName | Auth Patient            |
    Then the response status should be 201
    And the response should match:
      | patient.email | authpatient@example.com |
      | patient.displayName  | Auth Patient            |

    # Login
    When I log in as patient with:
      | email    | authpatient@example.com |
      | password | SuperSecret1            |
    Then the response status should be 200

    # Verify authenticated access
    When I call GET "/patient/me"
    Then the response status should be 200
    And the response field "patient.email" should be "authpatient@example.com"

    # Logout
    When I log out patient
    Then the response status should be 200

    # Verify session invalidated
    When I call GET "/patient/me"
    Then the response should be unauthorized
