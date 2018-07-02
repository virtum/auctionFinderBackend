package com.filocha.finder;

import https.webapi_allegro_pl.service.*;
import https.webapi_allegro_pl.service_php.ServicePort;
import https.webapi_allegro_pl.service_php.ServiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class AuctionFinderImpl implements AuctionFinder {

    private static final int POLAND_COUNTRY_CODE = 1;

    @Value("${userLogin}")
    private String userLogin;
    @Value("${userPassword}")
    private String userPassword;
    @Value("${webApiKey}")
    private String webApiKey;

    private ServicePort allegro;

    public AuctionFinderImpl() {
        final ServiceService allegroWebApiService = new ServiceService();
        allegro = allegroWebApiService.getServicePort();
    }

    //TODO remove shedule, move login method to finder when exception is thrown during find
    @PostConstruct
    //@Scheduled(fixedRate = 60 * 60 * 1000)
    private void login() {
        doLogin(userLogin, userPassword, webApiKey);
    }

    @Override
    public DoGetItemsListRequest createRequest(final String itemToFind) {
        final DoGetItemsListRequest itemsRequest = new DoGetItemsListRequest();
        itemsRequest.setCountryId(POLAND_COUNTRY_CODE);
        itemsRequest.setWebapiKey(webApiKey);

        final ArrayOfFilteroptionstype filter = new ArrayOfFilteroptionstype();
        FilterOptionsType fotcat = new FilterOptionsType();
        fotcat.setFilterId("search");

        final ArrayOfString categories = new ArrayOfString();
        categories.getItem().add(itemToFind);

        fotcat.setFilterValueId(categories);
        filter.getItem().add(fotcat);

        itemsRequest.setFilterOptions(filter);

        return itemsRequest;
    }

    @Override
    public CompletableFuture<List<ItemsListType>> findAuctions(final DoGetItemsListRequest request) {
        final CompletableFuture<List<ItemsListType>> result = new CompletableFuture<>();

        allegro.doGetItemsListAsync(request, args -> {
            try {
                result.complete(args.get().getItemsList().getItem());
            } catch (InterruptedException | ExecutionException e) {
                // TODO add logger
                e.printStackTrace();
            }
        });

        return result;
    }

    /**
     * Gets localVersion for Poland.
     *
     * @param webApiKey unique user key
     * @return localVersion for Poland
     */
    private long getLocalVersion(final String webApiKey) {
        final DoQueryAllSysStatusRequest params = new DoQueryAllSysStatusRequest();
        params.setCountryId(POLAND_COUNTRY_CODE);
        params.setWebapiKey(webApiKey);

        return allegro
                .doQueryAllSysStatus(params)
                .getSysCountryStatus()
                .getItem()
                .get(0)
                .getVerKey();
    }

    /**
     * Performs login operation to Allegro.
     *
     * @param userLogin    user login
     * @param userPassword user password
     * @param webApiKey    unique user key
     */
    private void doLogin(final String userLogin, final String userPassword, final String webApiKey) {
        final DoLoginRequest doLoginRequest = new DoLoginRequest();
        doLoginRequest.setUserLogin(userLogin);
        doLoginRequest.setUserPassword(userPassword);
        doLoginRequest.setCountryCode(POLAND_COUNTRY_CODE);
        doLoginRequest.setLocalVersion(getLocalVersion(webApiKey));
        doLoginRequest.setWebapiKey(webApiKey);

        allegro.doLogin(doLoginRequest);
    }

}
