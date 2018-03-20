package com.filocha.finder;

import https.webapi_allegro_pl.service.ItemsListType;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Value
@Builder
public class ResponseModel {

    private CompletableFuture<List<ItemsListType>> response;
    private String userEmail;
    private RequestModel request;
    private String item;

}
