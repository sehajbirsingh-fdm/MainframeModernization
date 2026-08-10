package com.modernizemainframe.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.modernizemainframe.api.CreateCustomerClient;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class CreateCustomerStepDefinitions {

    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern CUSTOMER_NUMBER_PATTERN = Pattern.compile("\\d{10}");
    private static final Pattern SORT_CODE_PATTERN = Pattern.compile("\\d{6}");
    
    private final CreateCustomerClient apiClient = new CreateCustomerClient();

    private Response response;
    private String firstCreatedCustomerNumber;

    @When("I submit create customer request variant {string}")
    public void iSubmitCreateCustomerRequestVariant(String variant) {
        Object payload = payloadForVariant(variant);
        response = apiClient.createCustomer(payload);
    }

    @When("I submit malformed create customer request body")
    public void iSubmitMalformedCreateCustomerRequestBody() {
        response = apiClient.createCustomerWithRawBody("{ this is not valid json }");
    }

    @When("I create two valid customers sequentially")
    public void iCreateTwoValidCustomersSequentially() {
        Response firstResponse = apiClient.createCustomer(payloadForVariant("valid"));
        firstCreatedCustomerNumber = asString(firstResponse.jsonPath().get("customerNumber"));
        response = apiClient.createCustomer(payloadForVariant("valid"));
    }

    @Then("the create customer response status code is {int}")
    public void theCreateCustomerResponseStatusCodeIs(int expectedStatusCode) {
        assertThat(response.getStatusCode())
                .as("Expected HTTP status code %d but got %d", expectedStatusCode, response.getStatusCode())
                .isEqualTo(expectedStatusCode);
    }

    @Then("the create customer response status code is one of {string}")
    public void theCreateCustomerResponseStatusCodeIsOneOf(String commaSeparatedCodes) {
        int actualStatusCode = response.getStatusCode();
        List<Integer> expectedCodes = Arrays.stream(commaSeparatedCodes.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        assertThat(expectedCodes)
                .as("Expected HTTP status code one of %s but got %d", expectedCodes, actualStatusCode)
                .contains(actualStatusCode);
    }

    @And("the create response contains generated customer identity")
    public void theCreateResponseContainsGeneratedCustomerIdentity() {
        String sortCode = asString(response.jsonPath().get("sortCode"));
        String customerNumber = asString(response.jsonPath().get("customerNumber"));

        assertThat(sortCode).isNotBlank();
        assertThat(SORT_CODE_PATTERN.matcher(sortCode).matches()).isTrue();

        assertThat(customerNumber).isNotBlank();
        assertThat(CUSTOMER_NUMBER_PATTERN.matcher(customerNumber).matches()).isTrue();
    }

    @And("the second created customer number is greater by one")
    public void theSecondCreatedCustomerNumberIsGreaterByOne() {
        String secondCustomerNumber = asString(response.jsonPath().get("customerNumber"));
        assertThat(firstCreatedCustomerNumber).isNotBlank();
        assertThat(secondCustomerNumber).isNotBlank();

        long first = Long.parseLong(firstCreatedCustomerNumber);
        long second = Long.parseLong(secondCustomerNumber);
        assertThat(second).isEqualTo(first + 1);
    }

    @And("legacy status commSuccess is {string} and commFailCode is {string}")
    public void legacyStatusCommSuccessIsAndCommFailCodeIs(String expectedSuccess, String expectedFailCode) {
        assertThat(asString(response.jsonPath().get("legacyStatus.commSuccess"))).isEqualTo(expectedSuccess);
        assertThat(asString(response.jsonPath().get("legacyStatus.commFailCode"))).isEqualTo(expectedFailCode);
    }

    @And("the create error response has standardized fields")
    public void theCreateErrorResponseHasStandardizedFields() {
        Object errorCode = response.jsonPath().get("error.code");
        assertThat(errorCode).isNotNull();
        assertThat(asString(response.jsonPath().get("error.message"))).isNotBlank();
        assertThat(asString(response.jsonPath().get("error.correlationId"))).isNotBlank();
        assertThat(asString(response.jsonPath().get("error.timestamp"))).isNotBlank();
    }

    @And("the create error legacy fail code is {string}")
    public void theCreateErrorLegacyFailCodeIs(String expectedFailCode) {
        System.out.println("----DEBUG RESPONSE-----"+response.asPrettyString());//TODO Remove debug line
        assertThat(asString(response.jsonPath().get("error.legacyFailCode"))).isEqualTo(expectedFailCode);
    }

    @And("the create response date field {string} is ISO yyyy-MM-dd")
    public void theCreateResponseDateFieldIsIsoYyyyMmDd(String fieldPath) {
        String value = asString(response.jsonPath().get(fieldPath));
        assertThat(value).isNotBlank();
        assertThat(ISO_DATE_PATTERN.matcher(value).matches())
                .as("Expected ISO yyyy-MM-dd at '%s' but got '%s'", fieldPath, value)
                .isTrue();
    }

    @And("the create response body path {string} equals {string}")
    public void theCreateResponseBodyPathEquals(String path, String expectedValue) {
        assertThat(asString(response.jsonPath().get(path))).isEqualTo(expectedValue);
    }

    private Object payloadForVariant(String variant) {
        Map<String, Object> payload = validPayload();

        switch (variant) {
            case "valid":
                return payload;
            case "blank-title":
                payload.put("title", "");
                return payload;
            case "invalid-title":
                payload.put("title", "Captain");
                return payload;
            case "dob-1601":
                payload.put("dateOfBirth", dateParts(1, 1, 1601));
                System.out.println("-----------PAYLOAD VIEW-----"+payload);
                return payload;
            case "dob-1600":
                payload.put("dateOfBirth", dateParts(31, 12, 1600));
                return payload;
            case "dob-invalid-calendar":
                payload.put("dateOfBirth", dateParts(31, 2, 1990));
                return payload;
            case "dob-future":
                LocalDate future = LocalDate.now().plusDays(1);
                payload.put("dateOfBirth", dateParts(future.getDayOfMonth(), future.getMonthValue(), future.getYear()));
                return payload;
            case "dob-over-150":
                LocalDate tooOld = LocalDate.now().minusYears(151);
                payload.put("dateOfBirth", dateParts(tooOld.getDayOfMonth(), tooOld.getMonthValue(), tooOld.getYear()));
                return payload;
            case "missing-dob-object":
                payload.remove("dateOfBirth");
                return payload;
            default:
                throw new IllegalArgumentException("Unsupported create-customer request variant: " + variant);
        }
    }

    private static Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Mr");
        payload.put("firstName", "John");
        payload.put("lastName", "Smith");
        payload.put("dateOfBirth", dateParts(15, 6, 1988));
        payload.put("createdDate", dateParts(4, 8, 2026));
        payload.put("phone", "07123456789");

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("line1", "1 Main St");
        address.put("line2", "Apt 2");
        address.put("city", "London");
        address.put("postcode", "E1 1AA");
        address.put("country", "UK");

        payload.put("address", address);
        payload.put("status", "ACTIVE");
        return payload;
    }

    private static Map<String, Object> dateParts(int day, int month, int year) {
        Map<String, Object> date = new LinkedHashMap<>();
        date.put("day", day);
        date.put("month", month);
        date.put("year", year);
        return date;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
