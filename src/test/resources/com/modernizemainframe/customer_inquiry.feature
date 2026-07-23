Feature: Customer Inquiry API

  Background:
    Given the API is running at localhost:8080

  @TC001 @FR001 @FR003 @FR008 @P1
  Scenario: CT-001 Specific customer found returns success payload
    When I request the customer with sort code "123456" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"

  @TC005 @FR001 @P2 @DBAgnostic
  Scenario: CT-002 Five-digit sort code validation returns deterministic error
    When I request the customer with sort code "12345" and customer number "0000000001"
    Then the response status code is 400

  @TC034 @FR001 @P2 @DBAgnostic
  Scenario: CT-003 Alphabetic customer number validation returns deterministic error
    When I request the customer with sort code "123456" and customer number "ABC0000001"
    Then the response status code is 400

  @TC002 @FR001 @FR003 @FR009 @P1
  Scenario: CT-004 Specific lookup not found maps to fail code 1
    When I request the customer with sort code "123456" and customer number "0000000999"
    Then the response status code is 200 or 404
    And the response body legacyStatus.inquirySuccess is "N"
    And the response body legacyStatus.inquiryFailCode is "1"

  @TC017 @FR006 @FR008 @P1 @DBAgnostic
  Scenario: CT-005 Latest sentinel request uses latest lookup mode
    When I request the customer with sort code "123456" and customer number "9999999999"
    Then the response status code is 200
    And the response body lookupMode is "LATEST"
    And the response body legacyStatus.inquirySuccess is "Y"

  @TC016 @FR004 @P2
  Scenario: CT-006 Random sentinel request returns deterministic API response
    When I request the customer with sort code "111111" and customer number "0000000000"
    Then the response status code is 200 or 404
