Feature: Customer Inquiry API

  Background:
    Given the API is running at localhost:8080

  @TC001 @FR001 @FR003 @FR008 @P1
  Scenario: Specific customer found returns success payload
    When I request the customer with sort code "123456" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"

  @TC005 @FR001 @P2 @DBAgnostic
  Scenario: Five-digit sort code validation returns deterministic error
    When I request the customer with sort code "12345" and customer number "0000000001"
    Then the response status code is 400

  @TC034 @FR001 @P2 @DBAgnostic
  Scenario: Alphabetic customer number validation returns deterministic error
    When I request the customer with sort code "123456" and customer number "ABC0000001"
    Then the response status code is 400

  @TC002 @FR001 @FR003 @FR009 @P1
  Scenario: Specific lookup not found maps to fail code 1
    When I request the customer with sort code "123456" and customer number "0000000999"
    Then the response status code is 200 or 404
    And the response body legacyStatus.inquirySuccess is "N"
    And the response body legacyStatus.inquiryFailCode is "1"

  @TC017 @FR006 @FR008 @P1 @DBAgnostic
  Scenario: Latest sentinel request uses latest lookup mode
    When I request the customer with sort code "123456" and customer number "9999999999"
    Then the response status code is 200
    And the response body lookupMode is "LATEST"
    And the response body legacyStatus.inquirySuccess is "Y"

  @TC016 @FR004 @P2
  Scenario: Random sentinel request returns deterministic API response
    When I request the customer with sort code "111111" and customer number "0000000000"
    Then the response status code is 200
    And the response body lookupMode is "RANDOM"

  @TC003 @FR001 @P2
  Scenario: Minimum sort code boundary returns specific customer
    When I request the customer with sort code "000000" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"
    And the response body "customer.sortCode" is "000000"

  @TC004 @FR001 @P2
  Scenario: Maximum sort code boundary returns specific customer
    When I request the customer with sort code "999999" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"
    And the response body "customer.sortCode" is "999999"

  @TC006 @FR001 @P2 @DBAgnostic
  Scenario: Seven-digit sort code validation returns deterministic error
    When I request the customer with sort code "0123456" and customer number "0000000001"
    Then the response status code is 400

  @TC007 @FR002 @P2
  Scenario: No default compatibility sort code when omitted
    When I request the compatibility customer endpoint with customer number "0000000001"
    Then the response status code is 400

  @TC008 @FR002 @P2
  Scenario: Blank sort code is rejected
    When I request the customer with sort code "" and customer number "0000000001"
    Then the response status code is 400

  @TC009 @FR002 @P2
  Scenario: Whitespace sort code is rejected
    When I request the customer with sort code "   " and customer number "0000000001"
    Then the response status code is 400

  @TC010 @FR003 @P1
  Scenario: Specific lookup mode for non-sentinel customer number
    When I request the customer with sort code "123456" and customer number "0000000002"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"

  @TC011 @FR003 @P1
  Scenario: Minimum non-random customer number remains specific lookup
    When I request the customer with sort code "123456" and customer number "0000000001"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.customerNumber" is "0000000001"

  @TC012 @FR003 @P1
  Scenario: Customer number below latest sentinel remains specific lookup
    When I request the customer with sort code "555555" and customer number "9999999998"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body "customer.customerNumber" is "9999999998"

  @TC013 @FR004 @FR008 @P1
  Scenario: Random sentinel lookup returns successful randomized customer
    When I request the customer with sort code "123456" and customer number "0000000000"
    Then the response status code is 200
    And the response body lookupMode is "RANDOM"
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"
    And the response body "customer.customerNumber" is not "0000000000"

  @TC014 @FR004 @P1
  Scenario: Random lookup lower-edge behavior remains successful
    When I request the customer with sort code "123456" and customer number "0000000000"
    Then the response status code is 200
    And the response body lookupMode is "RANDOM"
    And the response body legacyStatus.inquirySuccess is "Y"

  @TC015 @FR004 @P1
  Scenario: Random lookup high candidate behavior remains successful
    When I request the customer with sort code "123456" and customer number "0000000000"
    Then the response status code is 200
    And the response body lookupMode is "RANDOM"
    And the response body legacyStatus.inquirySuccess is "Y"

  @TC018 @FR006 @P1
  Scenario: Latest lookup with single customer returns that customer
    When I request the customer with sort code "222222" and customer number "9999999999"
    Then the response status code is 200
    And the response body lookupMode is "LATEST"
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body "customer.customerNumber" is "0000000001"

  @TC019 @FR006 @FR010 @P2
  Scenario: Latest lookup not found maps to fail code 9
    When I request the customer with sort code "654321" and customer number "9999999999"
    Then the response status code is one of "200,404"
    And the response body lookupMode is "LATEST"
    And the response body legacyStatus.inquirySuccess is "N"
    And the response body legacyStatus.inquiryFailCode is "9"
    And the response body "customer" is null

  @TC020 @FR007 @FR008 @FR009 @FR010 @P1
  Scenario Outline: Legacy status mapping stays consistent by scenario type
    When I request the customer with sort code "<sortCode>" and customer number "<customerNumber>"
    Then the response status code is one of "200,404"
    And the response body legacyStatus.inquirySuccess is "<inquirySuccess>"
    And the response body legacyStatus.inquiryFailCode is "<inquiryFailCode>"

    Examples:
      | sortCode | customerNumber | inquirySuccess | inquiryFailCode |
      | 123456   | 0000000001     | Y              | 0               |
      | 123456   | 0000000999     | N              | 1               |
      | 654321   | 9999999999     | N              | 9               |

  @TC021 @FR011 @P1
  Scenario: DB2 integer dates convert to ISO date strings
    When I request the customer with sort code "123456" and customer number "0000000003"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.dateOfBirth" is "1999-12-31"
    And the response body "customer.createdDate" is "2024-02-29"
    And the response body "customer.creditScoreReviewDate" is "2026-07-08"

  @TC022 @FR011 @P1
  Scenario: Earliest supported date boundary converts correctly
    When I request the customer with sort code "123456" and customer number "0000000004"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.dateOfBirth" is "1900-01-01"
    And the response body "customer.createdDate" is "1900-01-01"
    And the response body "customer.creditScoreReviewDate" is "1900-01-01"

  @TC023 @FR011 @P1
  Scenario: Invalid DB2 date fixture returns an error response
    When I request the customer with sort code "000013" and customer number "0000000005"
    Then the response status code is one of "400,500"

  @TC024 @FR012 @P1
  Scenario: ACTIVE status maps to API enum
    When I request the customer with sort code "123456" and customer number "0000000011"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.status" is "ACTIVE"

  @TC025 @FR012 @P1
  Scenario: INACTIVE status maps to API enum
    When I request the customer with sort code "123456" and customer number "0000000012"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.status" is "INACTIVE"

  @TC026 @FR012 @P1
  Scenario: SUSPENDED status maps to API enum
    When I request the customer with sort code "123456" and customer number "0000000013"
    Then the response status code is 200
    And the response body lookupMode is "SPECIFIC"
    And the response body "customer.status" is "SUSPENDED"

  @TC027 @FR012 @P1
  Scenario: Unsupported status fixture returns an error response
    When I request the customer with sort code "000013" and customer number "0000000014"
    Then the response status code is one of "400,500"

  @TC028 @FR013 @P1
  Scenario: Lookup request remains read-only at API level
    When I request the customer with sort code "123456" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"
    And the response body legacyStatus.inquiryFailCode is "0"

  @TC031 @FR001 @FR011 @FR012 @P1
  Scenario: Successful response includes mapped customer and address schema
    When I request the customer with sort code "123456" and customer number "0000000020"
    Then the response status code is 200
    And the response body "customer" is not null
    And the response body "customer.address" is not null
    And the response body "customer.address.line1" is "1 Main St"
    And the response body "customer.status" is "ACTIVE"

  @TC032 @FR001 @P2 @DBAgnostic
  Scenario: Eleven-digit customer number is rejected
    When I request the customer with sort code "123456" and customer number "00000000001"
    Then the response status code is 400

  @TC033 @FR001 @P2 @DBAgnostic
  Scenario: Nine-digit customer number is rejected
    When I request the customer with sort code "123456" and customer number "000000001"
    Then the response status code is 400
