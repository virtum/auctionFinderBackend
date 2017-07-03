package com.filocha.finder;

import https.webapi_allegro_pl.service.ItemsListType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ResponseModel {

    private CompletableFuture<List<ItemsListType>> response;
    private String userEmail;
    private RequestModel request;

    public ResponseModel(CompletableFuture<List<ItemsListType>> response, RequestModel request) {
        this.response = response;
        this.userEmail = request.getUserEmail();
        this.request = request;
    }

    public CompletableFuture<List<ItemsListType>> getResponse() {
        return response;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public RequestModel getRequest() {
        return request;
    }

}
