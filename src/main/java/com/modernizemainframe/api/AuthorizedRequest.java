package com.modernizemainframe.api;

import io.restassured.specification.RequestSpecification;

interface AuthorizedRequest extends BaseRequest{

    
    default RequestSpecification authorizedRequest(){
        final String VALID_TOKEN = "valid-inqacc-inquirer-token";
        return authorizedRequest(VALID_TOKEN);
    }

    default RequestSpecification authorizedRequest(String token){
        return jsonRequest()
                .header("Authorization", "Bearer " + token);    
    }
}
