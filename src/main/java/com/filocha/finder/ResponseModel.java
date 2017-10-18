package com.filocha.finder;

import https.webapi_allegro_pl.service.ItemsListType;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data //replace with Value
public class ResponseModel {

    private CompletableFuture<List<ItemsListType>> response;
    private String userEmail;
    private RequestModel request;
    private String item;

    //Remove ctor
    public ResponseModel(CompletableFuture<List<ItemsListType>> response, RequestModel request, String item) {
        this.response = response;
        this.userEmail = request.getUserEmail();
        this.request = request;
        this.item = item;
    }

}
