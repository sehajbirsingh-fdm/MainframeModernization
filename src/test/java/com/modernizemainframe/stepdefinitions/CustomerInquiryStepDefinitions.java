package com.modernizemainframe.stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;

import com.modernizemainframe.api.CustomerInquiryClient;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerInquiryStepDefinitions {

    private final CustomerInquiryClient apiClient = new CustomerInquiryClient();
    private Response response;

    @Before
    public void setUp() {
        apiClient.configure();
    }

    @After
    public void tearDown() {
        this.response = null;
    }

    @Given("the API is running at localhost:8080")
    public void theApiIsRunningAtLocalhost8080() {
        assertThat(apiClient.baseUri()).isEqualTo("http://localhost");
        assertThat(apiClient.port()).isEqualTo(8080);
    }

    @When("I request the customer with sort code {string} and customer number {string}")
    public void iRequestTheCustomerWithSortCodeAndCustomerNumber(String sortCode, String customerNumber) {
        this.response = apiClient.getCustomer(sortCode, customerNumber);
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
        Object rawValue = response.jsonPath().get("legacyStatus.inquiryFailCode");
        String actualValue = rawValue != null ? String.valueOf(rawValue) : null;
        assertThat(actualValue)
                .as("Expected legacyStatus.inquiryFailCode to be '%s' but got '%s'", expectedValue, actualValue)
                .isEqualTo(expectedValue);
    }

    @And("the response body lookupMode is {string}")
    public void theResponseBodyLookupModeIs(String expectedValue) {
        String actualValue = response.jsonPath().getString("lookupMode");
        assertThat(actualValue)
                .as("Expected lookupMode to be '%s' but got '%s'", expectedValue, actualValue)
                .isEqualTo(expectedValue);
    }
}
