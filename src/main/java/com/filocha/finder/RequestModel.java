package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import lombok.Data;

import java.util.Date;

@Data
public class RequestModel {
    private DoGetItemsListRequest request;
    private String userEmail;
    private boolean flag;
    private Date creationDate;
    private String item;

    //Remove ctor
    public RequestModel(DoGetItemsListRequest request, String userEmail, String item) {
        this.request = request;
        this.userEmail = userEmail;
        this.item = item;
    }

    //Remove ctor
    public RequestModel(DoGetItemsListRequest request, String userEmail, boolean flag, Date creationDate) {
        this.request = request;
        this.userEmail = userEmail;
        this.flag = flag;
        this.creationDate = creationDate;
    }

}
