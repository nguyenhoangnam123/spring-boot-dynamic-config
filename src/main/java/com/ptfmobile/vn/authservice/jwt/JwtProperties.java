package com.ptfmobile.vn.authservice.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class JwtProperties {
    @Value(value = "{jwt.secret-key}")
    private String secretKey;
}
