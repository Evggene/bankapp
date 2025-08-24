package org.bea.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "shared")
public class SharedAppProperties {
    private String gatewayBaseUrl = "http://gateway";
}
