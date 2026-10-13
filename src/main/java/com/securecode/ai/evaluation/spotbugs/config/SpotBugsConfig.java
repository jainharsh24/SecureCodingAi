package com.securecode.ai.evaluation.spotbugs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SpotBugsProperties.class)
public class SpotBugsConfig { }
