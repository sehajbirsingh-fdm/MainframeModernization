package com.modernizemainframe.api;

import io.restassured.response.Response;

public class CustomerAccountsInquiryClient implements BaseRequest {

    private static final String CUSTOMER_ACCOUNTS_ENDPOINT = "/api/v1/customers/{customerNumber}/accounts";
    private static final String VALID_TOKEN = "valid-inqacc-inquirer-token";
    

    public Response getCustomerAccounts(String customerNumber) {
        return jsonRequest()
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .when()
                .get(CUSTOMER_ACCOUNTS_ENDPOINT, customerNumber);
    }

    public Response getCustomerAccountsWithCorrelationId(String customerNumber, String correlationId) {
        return jsonRequest()
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .header("X-Correlation-Id", correlationId)
                .when()
                .get(CUSTOMER_ACCOUNTS_ENDPOINT, customerNumber);
    }
}
