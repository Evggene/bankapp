package org.bea.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class TransferAppProperties {
    private String gatewayBaseUrl = "http://gateway";
}
