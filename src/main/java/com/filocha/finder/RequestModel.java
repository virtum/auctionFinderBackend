package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;

public class RequestModel {

    private DoGetItemsListRequest request;
    private String userEmail;

    public DoGetItemsListRequest getRequest() {
        return request;
    }

    public void setRequest(DoGetItemsListRequest request) {
        this.request = request;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
