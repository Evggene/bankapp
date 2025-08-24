package org.bea.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(SharedAppProperties.class)
public class SharedAppAutoConfiguration {
}
