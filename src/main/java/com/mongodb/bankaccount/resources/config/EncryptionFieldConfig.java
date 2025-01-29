package com.mongodb.bankaccount.resources.config;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateEncryptedCollectionParams;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.*;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class EncryptionFieldConfig {

    protected boolean collectionExists(MongoDatabase db, String collectionName) {
        return db.listCollectionNames().into(new ArrayList<>()).contains(collectionName);
    }

    protected void createEncryptedCollection(MongoDatabase db, ClientEncryptionSettings clientEncryptionSettings) {
        var clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
        var encryptedCollectionParams = new CreateEncryptedCollectionParams("local")
            .masterKey(new BsonDocument());

        System.out.println("encryptFields()");
        System.out.println(encryptFields());
        var createCollectionOptions = new CreateCollectionOptions().encryptedFields(encryptFields());

        clientEncryption.createEncryptedCollection(db, "accounts", createCollectionOptions, encryptedCollectionParams);
    }

    protected static BsonDocument encryptFields() {
        return new BsonDocument().append("fields",
            new BsonArray(Arrays.asList(
                createEncryptedField("accountNumber", "string", equalityQueryType(), Optional.empty())
                ,createEncryptedField("cardVerificationCode", "int", equalityQueryType(), Optional.empty())
                ,createEncryptedField("accountBalance", "double", rangeQueryType(), Optional.empty())
                // ,createEncryptedField("phoneNumber", "string", equalityQueryType(), Optional.empty())

                // createEncryptedField("accountNumber", "string", equalityQueryType(), Optional.of("0a7fc6f9-a6f4-4f1a-8846-c40f5fe57736"))
                // ,createEncryptedField("cardVerificationCode", "int", equalityQueryType(), Optional.of("255caf5b-99db-4788-9b90-199d190a09ef"))
                // ,createEncryptedField("accountBalance", "double", rangeQueryType(), Optional.of("db881df4-2c77-4f5a-b5dd-72e85b2e2ad0"))
                // ,createEncryptedField("phoneNumber", "string", equalityQueryType(), Optional.of("db881df4-2c77-4f5a-b8dd-72e85b2e2ad0"))
                    
            )));
    }

    private static BsonDocument createEncryptedField(String path, String bsonType, BsonDocument query, Optional<String> keyIdUUID) {
        if (keyIdUUID.isEmpty()) {
            System.out.println("keyIdUUID is null");
            return new BsonDocument()
                .append("keyId", new BsonNull())
                .append("path", new BsonString(path))
                .append("bsonType", new BsonString(bsonType))
                .append("queries", query);
        } else {
            System.out.println("keyIdUUID is "+keyIdUUID.get());
            return new BsonDocument()
                .append("keyId", new BsonBinary(UUID.fromString(keyIdUUID.get())))
                .append("path", new BsonString(path))
                .append("bsonType", new BsonString(bsonType))
                .append("queries", query);
    }
    }

    private static BsonDocument rangeQueryType() {
        return new BsonDocument()
                .append("queryType", new BsonString("range"))
                .append("min", new BsonDouble(0))
                .append("max", new BsonDouble(999999999))
                .append("precision", new BsonInt32(2));
    }

    private static BsonDocument equalityQueryType() {
        return new BsonDocument().append("queryType", new BsonString("equality"));
    }
}
