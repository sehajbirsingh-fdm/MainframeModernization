Feature: Customer Accounts Inquiry API

  Background:
    Given the API is running at localhost:8080

  @TCACCCU001 @FR-OUT-001 @P1
  Scenario: Valid customer with accounts returns success
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "Y" fail code is "0" and customer found is "Y"
    And the response contains at most 20 accounts

  @TCACCCU002 @FR-OUT-002 @P1
  Scenario: Valid customer with zero accounts returns success
    When I request customer accounts with customer number "0000005678"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "Y" fail code is "0" and customer found is "Y"
    And the number of accounts is 0
    And the accounts array is empty

  @TCACCCU003 @FR-VAL-006 @FR-OUT-003 @P1
  Scenario Outline: Reserved values are handled as customer-not-found business outcomes
    # Covers: <testId>
    When I request customer accounts with customer number "<customerNumber>"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "N" fail code is "1" and customer found is "N"
    And the number of accounts is 0
    And the accounts array is empty

    Examples:
      | testId       | customerNumber |
      | TC-ACCCU-003 | 0000000000     |
      | TC-ACCCU-004 | 9999999999     |

  @TCACCCU005 @FR-VAL-003 @FR-ERR-001 @P1 @DBAgnostic
  Scenario Outline: Customer number length validation failures return 400
    # Covers: <testId>
    When I request customer accounts with customer number "<customerNumber>"
    Then the customer accounts inquiry response status code is 400

    Examples:
      | testId       | customerNumber |
      | TC-ACCCU-005 | 123456789      |
      | TC-ACCCU-006 | 12345678901    |

  @TCACCCU007 @FR-VAL-004 @FR-ERR-001 @P1 @DBAgnostic
  Scenario: Non-digit customer number returns validation failure
    When I request customer accounts with customer number "12345AB789"
    Then the customer accounts inquiry response status code is 400

  @TCACCCU008 @FR-VAL-001 @FR-VAL-002 @FR-ERR-001 @P1 @DBAgnostic
  Scenario: Blank-equivalent customer number returns validation failure
    When I request customer accounts with customer number "%20%20%20%20%20%20%20%20%20%20"
    Then the customer accounts inquiry response status code is 400

  @TCACCCU009 @FR-VAL-005 @FR-RESP-004 @P2
  Scenario: Leading zeroes are preserved in request and response
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And the accounts response customer number is "0000001234"

  @TCACCCU010 @FR-OUT-004 @P1 @Blocked
  Scenario: Retrieval open-stage failure maps to failCode 2
    Given blocked precondition "open-stage fault injection is unavailable"
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "N" fail code is "2" and customer found is "Y"

  @TCACCCU011 @FR-OUT-005 @P1 @Blocked
  Scenario: Retrieval fetch-stage failure maps to failCode 3
    Given blocked precondition "fetch-stage fault injection is unavailable"
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "N" fail code is "3" and customer found is "Y"

  @TCACCCU012 @FR-OUT-006 @P1 @Blocked
  Scenario: Retrieval close-stage failure maps to failCode 4
    Given blocked precondition "close-stage fault injection is unavailable"
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And legacy status success is "N" fail code is "4" and customer found is "Y"

  @TCACCCU013 @FR-ERR-002 @FR-ERR-004 @P1 @Blocked
  Scenario: Infrastructure failure returns 500 and omits business fields
    Given blocked precondition "infrastructure failure simulation is unavailable"
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 500
    And the infrastructure error response omits business outcome fields

  @TCACCCU014 @FR-BUS-003 @FR-RESP-001 @FR-RESP-002 @P2
  Scenario: Successful response has required account fields and fixed sort code
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And the accounts payload contains required account fields
    And the fixed sort code is "987654" for returned accounts

  @TCACCCU015 @FR-RESP-003 @P2
  Scenario: Account dates are returned as ISO yyyy-MM-dd
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And the account date fields are ISO yyyy-MM-dd

  @TCACCCU016 @FR-BUS-004 @P2 @Blocked
  Scenario: Response is capped to at most 20 accounts
    Given blocked precondition "over-20-accounts fixture is unavailable"
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And the response contains at most 20 accounts

  @TCACCCU017 @FR-BUS-005 @P2
  Scenario: Response assertions are independent of account ordering
    When I request customer accounts with customer number "0000001234"
    Then the customer accounts inquiry response status code is 200
    And the accounts payload contains required account fields
