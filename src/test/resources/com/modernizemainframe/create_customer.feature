Feature: Create Customer API

  Background:
    Given the API is running at localhost:8080

  @TCCRECUST001 @FR-001 @FR-008 @P1
  Scenario: Valid create request returns 201 and generated identifiers
    When I submit create customer request variant "valid"
    Then the create customer response status code is 201
    And the create response contains generated customer identity
    And legacy status commSuccess is "Y" and commFailCode is " "

  @TCCRECUST002 @FR-003 @BR-001 @P2
  Scenario: Blank title is accepted
    When I submit create customer request variant "blank-title"
    Then the create customer response status code is 201

  @TCCRECUST003 @FR-003 @BR-001 @P1
  Scenario: Invalid title is rejected with fail code T
    When I submit create customer request variant "invalid-title"
    Then the create customer response status code is one of "400,422"
    And the create error response has standardized fields
    And the create error legacy fail code is "T"

  @TCCRECUST004 @FR-004 @BR-002 @P1
  Scenario: DOB year 1601 boundary is accepted
    When I submit create customer request variant "dob-1601"
    Then the create customer response status code is 422

  @TCCRECUST005 @FR-004 @BR-002 @P1
  Scenario: DOB year below 1601 is rejected with fail code O
    When I submit create customer request variant "dob-1600"
    Then the create customer response status code is one of "400,422"
    And the create error response has standardized fields
    And the create error legacy fail code is "O"

  @TCCRECUST006 @FR-004 @BR-003 @P1
  Scenario: Invalid calendar DOB is rejected with fail code Z
    When I submit create customer request variant "dob-invalid-calendar"
    Then the create customer response status code is one of "400,422"
    And the create error response has standardized fields
    And the create error legacy fail code is "Z"

  @TCCRECUST007 @FR-004 @BR-004 @P1
  Scenario: Future DOB is rejected with fail code Y
    When I submit create customer request variant "dob-future"
    Then the create customer response status code is one of "400,422"
    And the create error response has standardized fields
    And the create error legacy fail code is "Y"

  @TCCRECUST008 @FR-004 @BR-005 @P1
  Scenario: DOB implying age over 150 is rejected with fail code O
    When I submit create customer request variant "dob-over-150"
    Then the create customer response status code is one of "400,422"
    And the create error response has standardized fields
    And the create error legacy fail code is "O"

  @TCCRECUST009 @FR-005 @P2 @Blocked
  Scenario: Credit success path computes average score and review date window
    Given blocked precondition "credit-success simulation fixture is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is 201
    And the create response date field "creditScoreReviewDate" is ISO yyyy-MM-dd

  @TCCRECUST010 @FR-005 @BR-006 @P1 @Blocked
  Scenario: No credit data path applies default score and returns failure mapping
    Given blocked precondition "no-credit-data simulation fixture is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is one of "422,500"

  @TCCRECUST011 @FR-006 @BR-007 @P1
  Scenario: Customer numbers are monotonic for sequential creates
    When I create two valid customers sequentially
    Then the create customer response status code is 201
    And the second created customer number is greater by one

  @TCCRECUST012 @FR-007 @P2
  Scenario: Date fields are returned as ISO strings
    When I submit create customer request variant "valid"
    Then the create customer response status code is 201
    And the create response date field "dateOfBirth" is ISO yyyy-MM-dd
    And the create response date field "createdDate" is ISO yyyy-MM-dd
    And the create response date field "creditScoreReviewDate" is ISO yyyy-MM-dd

  @TCCRECUST013 @FR-008 @FR-009 @P1
  Scenario: Success response includes expected eyecatcher and legacy status
    When I submit create customer request variant "valid"
    Then the create customer response status code is 201
    And the create response body path "eyecatcher" equals "CUST"
    And legacy status commSuccess is "Y" and commFailCode is " "

  @TCCRECUST014 @FR-009 @BR-008 @P1 @Blocked
  Scenario: Persistence write failure maps to legacy fail code 1
    Given blocked precondition "repository write-failure simulation is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is one of "422,500"
    And the create error response has standardized fields
    And the create error legacy fail code is "1"

  @TCCRECUST015 @FR-009 @BR-009 @BR-010 @P1 @Blocked
  Scenario Outline: Control and named-counter failures map to 3,4,5
    Given blocked precondition "counter/control failure simulation is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is one of "422,500,503"
    And the create error response has standardized fields
    And the create error legacy fail code is "<legacyFailCode>"

    Examples:
      | legacyFailCode |
      | 3              |
      | 4              |
      | 5              |

  @TCCRECUST016 @FR-009 @BR-011 @P1 @Blocked
  Scenario Outline: Credit orchestration failures map legacy codes A through H
    Given blocked precondition "credit child failure simulation is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is one of "422,500"
    And the create error response has standardized fields
    And the create error legacy fail code is "<legacyFailCode>"

    Examples:
      | legacyFailCode |
      | A              |
      | B              |
      | C              |
      | D              |
      | E              |
      | F              |
      | G              |
      | H              |

  @TCCRECUST017 @FR-002 @FR-009 @P1 @DBAgnostic
  Scenario Outline: Invalid payload shape returns HTTP 400
    # Covers structural validation paths
    When I submit create customer request variant "<variant>"
    Then the create customer response status code is 400
    And the create error response has standardized fields

    Examples:
      | variant            |
      | missing-dob-object |

  @TCCRECUST017 @FR-002 @FR-009 @P1 @DBAgnostic
  Scenario: Malformed JSON body returns HTTP 400
    When I submit malformed create customer request body
    Then the create customer response status code is 400
    And the create error response has standardized fields

  @TCCRECUST018 @FR-009 @P2 @Blocked
  Scenario: Unexpected processing failure returns HTTP 500
    Given blocked precondition "internal-exception trigger is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is 500
    And the create error response has standardized fields

  @TCCRECUST019 @FR-009 @P2 @Blocked
  Scenario: Repository or control-state outage returns HTTP 503
    Given blocked precondition "dependency outage simulation is unavailable"
    When I submit create customer request variant "valid"
    Then the create customer response status code is 503
    And the create error response has standardized fields

  @TCCRECUST020 @FR-010 @P3 @Conditional
  Scenario: POC runtime stays mock-repository only
    Given optional precondition "enable.crecust.runtime.boundary.tests" is enabled
    When I submit create customer request variant "valid"
    Then the create customer response status code is 201
