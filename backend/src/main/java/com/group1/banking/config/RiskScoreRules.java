package com.group1.banking.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties(prefix = "risk-score")
@Data
@NoArgsConstructor
public class RiskScoreRules {
    private double version;
    private InSufficientConditionConfig insufficientConditions;
    private Map<String, FactorConfig> factors = new HashMap<>();

    @Data
    public static class InSufficientConditionConfig {
        private int minMonths;
    }

    @Data
    public static class FactorConfig {
        private double weight;
        private List<Band> bands = new ArrayList<>();
    }

    @Data
    public static class Band {
        private double max;
        private int score;
        private String meaning;
    }

}
