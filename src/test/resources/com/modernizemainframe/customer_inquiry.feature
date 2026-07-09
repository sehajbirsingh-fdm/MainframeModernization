Feature: Customer Inquiry API

  Background:
    Given the API is running at localhost:8080

  Scenario: CT-001 Valid request returns HTTP 200
    When I request the customer with sort code "123456" and customer number "0000000001"
    Then the response status code is 200
    And the response body legacyStatus.inquirySuccess is "Y"

  Scenario: CT-002 Invalid sort code returns HTTP 400
    When I request the customer with sort code "ABCDEF" and customer number "0000000001"
    Then the response status code is 400

  Scenario: CT-003 Invalid customer number returns HTTP 400
    When I request the customer with sort code "123456" and customer number "ABC"
    Then the response status code is 400

  Scenario: CT-004 Not found returns HTTP 404
    When I request the customer with sort code "123456" and customer number "0000009999"
    Then the response status code is 404
    And the response body legacyStatus.inquiryFailCode is "1"

  Scenario: CT-005 Latest request returns HTTP 200
    When I request the customer with sort code "123456" and customer number "9999999999"
    Then the response status code is 200
    And the response contains the latest customer

  Scenario: CT-006 Random request returns HTTP 200 or 404
    When I request the customer with sort code "123456" and customer number "0000000000"
    Then the response status code is 200 or 404
