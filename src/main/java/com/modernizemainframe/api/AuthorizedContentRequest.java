package com.modernizemainframe.api;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public interface AuthorizedContentRequest extends AuthorizedRequest {

    default RequestSpecification authorizedContentRequest(){
        return authorizedRequest()
            .contentType(ContentType.JSON);
    }

}
