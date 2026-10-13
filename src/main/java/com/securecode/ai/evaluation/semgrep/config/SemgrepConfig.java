package com.securecode.ai.evaluation.semgrep.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SemgrepProperties.class)
public class SemgrepConfig { }
