package com.filocha.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class StorageConfiguration {

    @Autowired
    private MongoOperations mongoOperations;

    // Temporary method for further refactor
    public void saveUser(String userName) {
        UserModel user = new UserModel(userName);
        mongoOperations.save(user);
    }

    public UserModel findUser(String userName) {
        Query query = new Query(Criteria.where("name").is(userName));
        return mongoOperations.findOne(query, UserModel.class);
    }

}

@Document(collection = "users")
class UserModel {

    @Id
    private String id;
    private String name;

    public UserModel(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
