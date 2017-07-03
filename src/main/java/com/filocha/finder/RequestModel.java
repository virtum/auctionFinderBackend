package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;

import java.util.Date;

public class RequestModel {
    private DoGetItemsListRequest request;
    private String userEmail;
    private boolean flag;
    private Date creationDate;

    public RequestModel(DoGetItemsListRequest request, String userEmail) {
        this.request = request;
        this.userEmail = userEmail;
    }

    public RequestModel(DoGetItemsListRequest request, String userEmail, boolean flag, Date creationDate) {
        this.request = request;
        this.userEmail = userEmail;
        this.flag = flag;
        this.creationDate = creationDate;
    }

    public DoGetItemsListRequest getRequest() {
        return request;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public boolean isFlag() {
        return flag;
    }

    public Date getCreationDate() {
        return creationDate;
    }

}
