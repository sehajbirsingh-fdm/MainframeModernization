package com.modernizemainframe.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.modernizemainframe.api.CustomerAccountsInquiryClient;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class CustomerAccountsInquiryStepDefinitions {

    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final CustomerAccountsInquiryClient apiClient = new CustomerAccountsInquiryClient();
    private Response response;

    @When("I request customer accounts with customer number {string}")
    public void iRequestCustomerAccountsWithCustomerNumber(String customerNumber) {
        response = apiClient.getCustomerAccounts(customerNumber);
    }

    @Then("the customer accounts inquiry response status code is {int}")
    public void theCustomerAccountsInquiryResponseStatusCodeIs(int expectedStatusCode) {
        assertThat(response.getStatusCode())
                .as("Expected HTTP status code %d but got %d", expectedStatusCode, response.getStatusCode())
                .isEqualTo(expectedStatusCode);
    }

    @Then("the customer accounts inquiry response status code is one of {string}")
    public void theCustomerAccountsInquiryResponseStatusCodeIsOneOf(String commaSeparatedCodes) {
        int actualStatusCode = response.getStatusCode();
        List<Integer> expectedCodes = Arrays.stream(commaSeparatedCodes.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        assertThat(expectedCodes)
                .as("Expected HTTP status code one of %s but got %d", expectedCodes, actualStatusCode)
                .contains(actualStatusCode);
    }

    @And("legacy status success is {string} fail code is {string} and customer found is {string}")
    public void legacyStatusSuccessIsFailCodeIsAndCustomerFoundIs(
            String success,
            String failCode,
            String customerFound
    ) {
        assertThat(asString(response.jsonPath().get("legacyStatus.success"))).isEqualTo(success);
        assertThat(asString(response.jsonPath().get("legacyStatus.failCode"))).isEqualTo(failCode);
        assertThat(asString(response.jsonPath().get("legacyStatus.customerFound"))).isEqualTo(customerFound);
    }

    @And("the accounts response customer number is {string}")
    public void theAccountsResponseCustomerNumberIs(String expectedCustomerNumber) {
        assertThat(asString(response.jsonPath().get("customerNumber"))).isEqualTo(expectedCustomerNumber);
    }

    @And("the number of accounts is {int}")
    public void theNumberOfAccountsIs(int expectedCount) {
        Integer count = response.jsonPath().getInt("numberOfAccounts");
        assertThat(count).isEqualTo(expectedCount);
    }

    @And("the accounts array is empty")
    public void theAccountsArrayIsEmpty() {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        assertThat(accounts).isEmpty();
    }

    @And("the accounts array is not empty")
    public void theAccountsArrayIsNotEmpty() {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        assertThat(accounts).isNotEmpty();
    }

    @And("the response contains at most {int} accounts")
    public void theResponseContainsAtMostAccounts(int maxAccounts) {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        assertThat(accounts.size()).isLessThanOrEqualTo(maxAccounts);
        Integer numberOfAccounts = response.jsonPath().getInt("numberOfAccounts");
        assertThat(numberOfAccounts).isLessThanOrEqualTo(maxAccounts);
    }

    @And("the accounts payload contains required account fields")
    public void theAccountsPayloadContainsRequiredAccountFields() {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        if (accounts.isEmpty()) {
            return;
        }

        Map<String, Object> account = accounts.get(0);
        assertThat(account).containsKeys(
                "eyecatcher",
                "customerNumber",
                "sortCode",
                "accountNumber",
                "accountType",
                "interestRate",
                "openedDate",
                "overdraftLimit",
                "lastStatementDate",
                "nextStatementDate",
                "availableBalance",
                "actualBalance"
        );
    }

    @And("the account date fields are ISO yyyy-MM-dd")
    public void theAccountDateFieldsAreIsoYyyyMmDd() {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        if (accounts.isEmpty()) {
            return;
        }

        Map<String, Object> account = accounts.get(0);
        assertIsoDate(account.get("openedDate"));
        assertIsoDate(account.get("lastStatementDate"));
        assertIsoDate(account.get("nextStatementDate"));
    }

    @And("the infrastructure error response omits business outcome fields")
    public void theInfrastructureErrorResponseOmitsBusinessOutcomeFields() {
        Object legacyStatus = response.jsonPath().get("legacyStatus");
        Object customerNumber = response.jsonPath().get("customerNumber");
        Object numberOfAccounts = response.jsonPath().get("numberOfAccounts");
        Object accounts = response.jsonPath().get("accounts");

        assertThat(legacyStatus).isNull();
        assertThat(customerNumber).isNull();
        assertThat(numberOfAccounts).isNull();
        assertThat(accounts).isNull();
    }

    @And("the fixed sort code is {string} for returned accounts")
    public void theFixedSortCodeIsForReturnedAccounts(String expectedSortCode) {
        List<Map<String, Object>> accounts = response.jsonPath().getList("accounts");
        assertThat(accounts).isNotNull();
        if (accounts.isEmpty()) {
            return;
        }
        for (Map<String, Object> account : accounts) {
            assertThat(asString(account.get("sortCode"))).isEqualTo(expectedSortCode);
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static void assertIsoDate(Object rawValue) {
        String value = asString(rawValue);
        assertThat(value).isNotBlank();
        assertThat(ISO_DATE_PATTERN.matcher(value).matches())
                .as("Expected ISO yyyy-MM-dd date but got '%s'", value)
                .isTrue();
    }
}
