package com.ptfmobile.vn.authservice.config;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MongoDbConfig {
    final Logger logger = LoggerFactory.getLogger(MongoDbConfig.class);
    @Value("${mongo.dbname}")
    private String dbName;
    @Value("${mongo.connectionstr}")
    private String connectionstr;
    @Bean
    public MongoDatabase getDatabase() {
        try {
            logger.info("Setting up MongoDbAccess");
            MongoDatabase database;
            ConnectionString connectionString = new ConnectionString(connectionstr);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            com.mongodb.client.MongoClient mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(dbName);

            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            return database.withCodecRegistry(pojoCodecRegistry);
        } catch (Exception ex) {
            logger.error("", ex);
            return null;
        }
    }
}