package com.filocha;


import com.filocha.finder.ResponseModel;
import com.filocha.storage.AuctionModel;
import com.filocha.storage.SubscriberModel1;
import com.filocha.storage.SubscriptionStorage;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TempTest {
    public static Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

    @Test
    public void shouldFindUserEmailInList() {
        SubscriberModel1 model1 = new SubscriberModel1();
        model1.setEmail("model1");

        SubscriberModel1 model2 = new SubscriberModel1();
        model2.setEmail("model2");

        SubscriptionStorage.userAuctions1.add(model1);
        SubscriptionStorage.userAuctions1.add(model2);

        Optional<SubscriberModel1> result = SubscriptionStorage.findSubscriberByEmail("model2");

        assertTrue(result.isPresent());
    }

    @Test
    public void shouldFindItem() {
        AuctionModel item1 = new AuctionModel();
        item1.setItemName("test1");

        AuctionModel item2 = new AuctionModel();
        item2.setItemName("test2");

        SubscriberModel1 model = new SubscriberModel1();
        model.setEmail("model");
        model.setAuctions(new ArrayList<>(Arrays.asList(item1, item2)));

        SubscriptionStorage.userAuctions1.add(model);

        //assertTrue(SubscriptionStorage.getAuction("model", "test2"));

    }

    @Test
    public void shouldAddNewValues() {
        String user = "testUser";
        String item = "testItem";

        Map<String, List<String>> itemWithUrl = new HashMap<>();
        itemWithUrl.put(item, new ArrayList<>(Arrays.asList("1", "2")));

        userAuctions.put(user, itemWithUrl);

        updateUserUrls(user, item, Arrays.asList("2", "4"));

        assertEquals(3, userAuctions.get(user).get(item).size());

    }

    private static List<String> handleUserAuctions(ResponseModel response, String item) {
        List<String> foundUrls = prepareAuctionsIdList(response);
        String userEmail = response.getUserEmail();

        if (!userAuctions.containsKey(userEmail)) {
            userAuctions.put(userEmail, Collections.singletonMap(item, foundUrls));
            return Collections.emptyList();
        }
        return updateUserUrls(userEmail, item, foundUrls);
    }

    private static List<String> updateUserUrls(String userEmail, String item, List<String> newUrls) {
        List<String> currentUrls = userAuctions.get(userEmail).get(item);

        List<String> urlToAdd = newUrls
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());

        currentUrls.addAll(urlToAdd);
        Map<String, List<String>> newMap = Collections.singletonMap(item, currentUrls);

        userAuctions.put(userEmail, newMap);

        return urlToAdd;
    }

    @SneakyThrows
    private static List<String> prepareAuctionsIdList(ResponseModel response) {
        List<Long> auctionsId = new ArrayList<>();

        //FIXME get? wtf?
        auctionsId.addAll(response.getResponse().get()
                .stream()
                .map(ItemsListType::getItemId)
                .collect(Collectors.toList()));

        return prepareTestMessage(auctionsId);
    }

    private static List<String> prepareTestMessage(List<Long> urlsId) {
        return urlsId
                .stream()
                .map(url -> "http://allegro.pl/i" + url + ".html\n")
                .collect(Collectors.toList());
    }
}


