package com.mongodb.bankaccount.resources;

import com.mongodb.bankaccount.domain.BankAccount;
import com.mongodb.bankaccount.domain.BankAccountPort;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

@Repository
public class BankAccountRepository implements BankAccountPort {
    private static final Logger logger = LoggerFactory.getLogger(BankAccountRepository.class);

    private static final String COLLECTION_NAME = "accounts";
    private final MongoClient mongoClient;
    private final MongoCollection<BankAccountEntity> collection;

    BankAccountRepository(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        this.mongoClient = mongoClient;
        this.collection = mongoDatabase.getCollection(COLLECTION_NAME, BankAccountEntity.class);
    }

    @Override
    public String insert(BankAccount bankAccount) {
        try {
            InsertOneResult insertOneResult = collection.insertOne(bankAccount.toEntity());
            ObjectId result = Objects.requireNonNull(insertOneResult.getInsertedId()).asObjectId().getValue();

            logger.info("{} was created", result);

            return result.toHexString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error inserting bank account", e);
        }
    }

    @Override
    public List<BankAccount> find() {
        try {
            ArrayList<BankAccountEntity> bankAccounts = new ArrayList<>();
            collection.find().into(bankAccounts);

            return bankAccounts.stream()
                    .map(BankAccountEntity::toDomain)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error finding bank account", e);
        }
    }

    @Override
    public List<BankAccount> findByBalanceGreaterThan(double value) {
        try {
            ArrayList<BankAccountEntity> bankAccounts = new ArrayList<>();
            collection.find(gt("accountBalance", value)).into(bankAccounts);

            return bankAccounts.stream()
                    .map(BankAccountEntity::toDomain)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error finding bank account", e);
        }
    }

    @Override
    public BankAccount findByAccountNumber(String accountNumber) {
        try {
            BankAccountEntity result = collection.find(eq("accountNumber", accountNumber)).first();

            if (result == null) {
                return null;
            }

            return result.toDomain();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error finding bank account", e);
        }

    }

    @Override
    public Object copyData(String sourceNamespace, String targetNamespace) {
        String[] sourceParts = sourceNamespace.split("\\.");
        String[] targetParts = targetNamespace.split("\\.");

        if (sourceParts.length != 2 || targetParts.length != 2) {
            throw new IllegalArgumentException("Invalid namespace format. Expected format: database.collection");
        }

        MongoDatabase sourceDatabase = mongoClient.getDatabase(sourceParts[0]);
        MongoCollection<Document> sourceCollection = sourceDatabase.getCollection(sourceParts[1]);

        MongoDatabase targetDatabase = mongoClient.getDatabase(targetParts[0]);
        MongoCollection<Document> targetCollection = targetDatabase.getCollection(targetParts[1]);

        List<Document> documents = sourceCollection.find().into(new ArrayList<>());
        return targetCollection.insertMany(documents);
    }
}
