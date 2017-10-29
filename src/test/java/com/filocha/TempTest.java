package com.filocha;


import com.filocha.finder.ResponseModel;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TempTest {
    public static Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

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


