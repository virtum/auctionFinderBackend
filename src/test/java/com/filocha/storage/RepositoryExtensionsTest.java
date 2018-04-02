package com.filocha.storage;

import com.filocha.MongoTestConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@ContextConfiguration(classes = {MongoTestConfig.class})
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = "classpath:applicationTest.properties")
public class RepositoryExtensionsTest {

    @Autowired
    private MongoTemplate mongoTemplate;


}