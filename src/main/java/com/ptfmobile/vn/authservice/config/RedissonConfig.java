package com.ptfmobile.vn.authservice.config;

import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HuyNV
 */
@Configuration
public class RedissonConfig {

    @Value("${redis.redisson.config}")
    private String configYaml;

    @Bean
    public RedissonClient getRedissonClient(){
        Config config;
        try {
            config = Config.fromYAML(configYaml);
            return Redisson.create(config);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
