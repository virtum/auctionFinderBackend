package com.filocha.finder;

import com.filocha.MongoTestConfig;
import com.filocha.handler.JmsTestConfig;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmsTestConfig.class, MongoTestConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:applicationTest.properties")
public class AuctionFinderImplTest {

    @Autowired
    private AuctionFinder auctionFinder;

    @Test
    public void shouldFindAnyAuction() throws ExecutionException, InterruptedException {
        final DoGetItemsListRequest request = auctionFinder.createRequest("nokia");

        final List<ItemsListType> result = auctionFinder.findAuctions(request).get();

        assertThat(result.size(), greaterThan(0));
    }

}
