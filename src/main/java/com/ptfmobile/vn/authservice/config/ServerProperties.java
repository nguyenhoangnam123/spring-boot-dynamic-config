package com.ptfmobile.vn.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mongo")
public class ServerProperties {

    private List<Map<String, String>> addresses;

    public List<Map<String, String>> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Map<String, String>> addresses) {
        this.addresses = addresses;
    }
}
