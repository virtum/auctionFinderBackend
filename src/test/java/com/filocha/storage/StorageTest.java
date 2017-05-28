package com.filocha.storage;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageTest {

    @Autowired
    private StorageConfiguration storageConfiguration;

    @Test
    public void shouldAddAndFindUser() {
        String userName = "Pawel";
        storageConfiguration.saveUser(userName);

        UserModel user = storageConfiguration.findUser(userName);

        assertThat(user.getName(), equalTo(userName));
    }

}
