package com.filocha.finder;

import https.webapi_allegro_pl.service.*;
import https.webapi_allegro_pl.service_php.ServicePort;
import https.webapi_allegro_pl.service_php.ServiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Component
public class AuctionFinderImpl implements AuctionFinder {

    @Value("${userLogin}")
    private String userLogin;

    @Value("${userPassword}")
    private String userPassword;

    @Value("${webApiKey}")
    private String webApiKey;

    ServiceService allegroWebApiService;
    ServicePort allegro;

    private CopyOnWriteArrayList<CompletableFuture<List<ItemsListType>>> responses = new CopyOnWriteArrayList<>();

    public AuctionFinderImpl() {
        allegroWebApiService = new ServiceService();
        //allegroWebApiService.setExecutor(Executors.newFixedThreadPool(400));
        allegro = allegroWebApiService.getServicePort();
    }

    //TODO remove shedule, move login method to finder when exception is thrown during find
    @PostConstruct
    //@Scheduled(fixedRate = 60 * 60 * 1000)
    private void login() {
        doLogin(userLogin, userPassword, webApiKey);

    }

    @Override
    public DoGetItemsListRequest createRequest(String keyword) {
        DoGetItemsListRequest itemsreq = new DoGetItemsListRequest();
        itemsreq.setCountryId(1);
        itemsreq.setWebapiKey(webApiKey);
        itemsreq.setResultSize(8);

        ArrayOfFilteroptionstype filter = new ArrayOfFilteroptionstype();
        FilterOptionsType fotcat = new FilterOptionsType();
        fotcat.setFilterId("search");

        ArrayOfString categories = new ArrayOfString();
        categories.getItem().add(keyword);

        fotcat.setFilterValueId(categories);
        filter.getItem().add(fotcat);

        itemsreq.setFilterOptions(filter);

        return itemsreq;
    }

    @Override
    public CompletableFuture<List<ItemsListType>> findAuctions(DoGetItemsListRequest request) {

        CompletableFuture<List<ItemsListType>> result = new CompletableFuture<>();

        allegro.doGetItemsListAsync(request, args -> {
            try {
                result.complete(args.get().getItemsList().getItem());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    private long getLocalVersion(String webApiKey) {
        int countryCode = 1;

        DoQueryAllSysStatusRequest params = new DoQueryAllSysStatusRequest();
        params.setCountryId(countryCode);
        params.setWebapiKey(webApiKey);
        DoQueryAllSysStatusResponse response = allegro.doQueryAllSysStatus(params);

        return response.getSysCountryStatus().getItem().get(0).getVerKey();
    }

    private void doLogin(String userLogin, String userPassword, String webApiKey) {
        ServiceService allegroWebApiService = new ServiceService();

        ServicePort allegro = allegroWebApiService.getServicePort();

        DoLoginRequest doLoginRequest = new DoLoginRequest();
        doLoginRequest.setUserLogin(userLogin);
        doLoginRequest.setUserPassword(userPassword);
        doLoginRequest.setCountryCode(1);
        doLoginRequest.setLocalVersion(getLocalVersion(webApiKey));
        doLoginRequest.setWebapiKey(webApiKey);

        allegro.doLogin(doLoginRequest);
    }


}
