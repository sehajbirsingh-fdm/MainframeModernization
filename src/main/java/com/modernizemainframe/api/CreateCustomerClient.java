package com.modernizemainframe.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class CreateCustomerClient implements AuthorizedContentRequest {

    private static final String CREATE_CUSTOMER_ENDPOINT = "/v1/customers";
    private static final String VALID_TOKEN = "valid-inqacc-inquirer-token";

    public Response createCustomer(Object requestBody) {
        return authorizedContentRequest()
                .body(requestBody)
                .when()
                .post(CREATE_CUSTOMER_ENDPOINT);
    }

    public Response createCustomerWithCorrelationId(Object requestBody, String correlationId) {
        return authorizedContentRequest()
                .header("X-Correlation-Id", correlationId)
                .body(requestBody)
                .when()
                .post(CREATE_CUSTOMER_ENDPOINT);
    }

    public Response createCustomerWithRawBody(String rawBody) {
        return authorizedContentRequest()
                .body(rawBody)
                .when()
                .post(CREATE_CUSTOMER_ENDPOINT);
    }
}
