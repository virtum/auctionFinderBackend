package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestModel {
    private DoGetItemsListRequest request;
    private String userEmail;
    private String item;
}
