package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;

import java.util.Date;

public class RequestModel {
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        RequestModel that = (RequestModel) o;
//
//        if (flag != that.flag) return false;
//        if (request != null ? !request.equals(that.request) : that.request != null) return false;
//        if (userEmail != null ? !userEmail.equals(that.userEmail) : that.userEmail != null) return false;
//        return creationDate != null ? creationDate.equals(that.creationDate) : that.creationDate == null;
//    }

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
