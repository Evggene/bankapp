package org.bea.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TransferAppProperties {
    private String gatewayBaseUrl = "http://gateway";

    public String getGatewayBaseUrl() {
        return gatewayBaseUrl;
    }
    public void setGatewayBaseUrl(String gatewayBaseUrl) {
        this.gatewayBaseUrl = gatewayBaseUrl;
    }
}
