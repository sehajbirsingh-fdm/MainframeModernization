package com.modernizemainframe.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CustomerInquiryClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public void configure() {
        String host = System.getProperty("api.host", DEFAULT_HOST);
        int port = Integer.getInteger("api.port", DEFAULT_PORT);
        RestAssured.baseURI = "http://" + host;
        RestAssured.port = port;
    }

    public String baseUri() {
        return RestAssured.baseURI;
    }

    public int port() {
        return RestAssured.port;
    }

    public Response getCustomer(String sortCode, String customerNumber) {
        return RestAssured
                .given()
                .accept("application/json")
                .when()
                .get("/api/v1/customers/{sortCode}/{customerNumber}", sortCode, customerNumber);
    }

    public Response getCustomerCompatibility(String customerNumber) {
        return RestAssured
                .given()
                .accept("application/json")
                .queryParam("customerNumber", customerNumber)
                .when()
                .get("/api/v1/customers");
    }

    public Response getCustomerCompatibility(String sortCode, String customerNumber) {
        return RestAssured
                .given()
                .accept("application/json")
                .queryParam("sortCode", sortCode)
                .queryParam("customerNumber", customerNumber)
                .when()
                .get("/api/v1/customers");
    }
}
