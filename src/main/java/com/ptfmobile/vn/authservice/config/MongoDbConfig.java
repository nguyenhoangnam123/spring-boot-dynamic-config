package com.ptfmobile.vn.authservice.config;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ptfmobile.vn.common.AppUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MongoDbConfig {

    final Logger logger = LoggerFactory.getLogger(MongoDbConfig.class);

    @Value("${mongo.dbname}")
    private String dbName;

    @Value("${mongo.username}")
    private String userName;

    @Value("${mongo.password}")
    private String password;

    @Value("${mongo.connectionstr}")
    private String connectionstr;

    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public MongoDatabase getDatabase() {
        try {
            logger.info("Setting up MongoDbAccess");
            if (isNull(serverProperties)) {
                return null;
            }

            MongoDatabase database;
            if (!AppUtils.isNullOrEmpty(connectionstr)) {
                ConnectionString connectionString = new ConnectionString(connectionstr);
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .build();
                com.mongodb.client.MongoClient mongoClient = MongoClients.create(settings);
                database = mongoClient.getDatabase(dbName);
            } else {
                List<ServerAddress> addresses = serverProperties.getAddresses().stream()
                        .map(property -> new ServerAddress(property.get("host"), Integer.parseInt(property.get("port"))))
                        .collect(toList());

                MongoCredential credential = MongoCredential.createScramSha1Credential(
                        userName,
                        dbName,
                        password.toCharArray());

                com.mongodb.client.MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(addresses))
                        .credential(credential)
                        .build());

                database = mongoClient.getDatabase(dbName);

            }
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            return database.withCodecRegistry(pojoCodecRegistry);
        } catch (Exception ex) {
            logger.error("", ex);
            return null;
        }
    }
}

