package com.modernizemainframe.stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private Response response;

    @Before
    public void setUp() {
        String host = System.getProperty("api.host", DEFAULT_HOST);
        int port = Integer.getInteger("api.port", DEFAULT_PORT);
        RestAssured.baseURI = "http://" + host;
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        this.response = null;
    }

    @Given("the API is running at localhost:8080")
    public void theApiIsRunningAtLocalhost8080() {
        assertThat(RestAssured.baseURI).isEqualTo("http://localhost");
        assertThat(RestAssured.port).isEqualTo(8080);
    }

    @When("I request the customer with sort code {string} and customer number {string}")
    public void iRequestTheCustomerWithSortCodeAndCustomerNumber(String sortCode, String customerNumber) {
        this.response = RestAssured
                .given()
                .accept("application/json")
                .when()
                .get("/api/v1/customers/{sortCode}/{customerNumber}", sortCode, customerNumber);
    }

    @Then("the response status code is {int}")
    public void theResponseStatusCodeIs(int expectedStatusCode) {
        assertThat(response.getStatusCode())
                .as("Expected HTTP status code %d but got %d", expectedStatusCode, response.getStatusCode())
                .isEqualTo(expectedStatusCode);
    }

    @Then("the response status code is {int} or {int}")
    public void theResponseStatusCodeIsOr(int expectedStatusCode1, int expectedStatusCode2) {
        int actualStatusCode = response.getStatusCode();
        List<Integer> expectedCodes = List.of(expectedStatusCode1, expectedStatusCode2);
        assertThat(expectedCodes)
                .as("Expected HTTP status code %s but got %d", expectedCodes, actualStatusCode)
                .contains(actualStatusCode);
    }

    @And("the response body legacyStatus.inquirySuccess is {string}")
    public void theResponseBodyLegacyStatusInquirySuccessIs(String expectedValue) {
        String actualValue = response.jsonPath().getString("legacyStatus.inquirySuccess");
        assertThat(actualValue)
                .as("Expected legacyStatus.inquirySuccess to be '%s' but got '%s'", expectedValue, actualValue)
                .isEqualTo(expectedValue);
    }

    @And("the response body legacyStatus.inquiryFailCode is {string}")
    public void theResponseBodyLegacyStatusInquiryFailCodeIs(String expectedValue) {
        // The API may serialize the fail code as a string or a number; coerce to string for comparison.
        Object rawValue = response.jsonPath().get("legacyStatus.inquiryFailCode");
        String actualValue = rawValue != null ? String.valueOf(rawValue) : null;
        assertThat(actualValue)
                .as("Expected legacyStatus.inquiryFailCode to be '%s' but got '%s'", expectedValue, actualValue)
                .isEqualTo(expectedValue);
    }

    @And("the response contains the latest customer")
    public void theResponseContainsTheLatestCustomer() {
        assertThat(response.getStatusCode())
                .as("Expected HTTP 200 for latest customer but got %d", response.getStatusCode())
                .isEqualTo(200);

        // Assert that a customer object is present in the response body.
        // The exact field name depends on the API contract; adjust if the root field is named differently.
        Object customer = response.jsonPath().get("customer");
        assertThat(customer)
                .as("Expected a customer object in the response body for the latest customer request")
                .isNotNull();
    }
}
