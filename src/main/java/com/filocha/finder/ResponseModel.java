package com.filocha.finder;

import https.webapi_allegro_pl.service.ItemsListType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ResponseModel {

    private CompletableFuture<List<ItemsListType>> response;
    private String userEmail;

    public CompletableFuture<List<ItemsListType>> getResponse() {
        return response;
    }

    public void setResponse(CompletableFuture<List<ItemsListType>> response) {
        this.response = response;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
