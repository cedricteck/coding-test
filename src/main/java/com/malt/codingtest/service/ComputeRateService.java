package com.malt.codingtest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.codingtest.model.ApplicableRate;
import com.malt.codingtest.model.CalculRequest;
import com.malt.codingtest.model.Rate;
import com.malt.codingtest.model.Rule;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ComputeRateService {

    public ApplicableRate findApplicableRate(CalculRequest calculRequest) {
        String ruleString = "{\n" +
                "  \"@or\": [\n" +
                "    {\n" +
                "      \"@mission.duration\": {\n" +
                "        \"gt\": \"2months\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"@commercialRelationship.duration\": {\n" +
                "        \"gt\": \"2months\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"@client.location\": {\n" +
                "    \"country\": \"ES\"\n" +
                "  },\n" +
                "  \"@freelancer.location\": {\n" +
                "    \"country\": \"ES\"\n" +
                "  }\n" +
                "}";
        Rule rule = new Rule();
        Rate rate = new Rate();
        rate.setPercent(8L);
        rule.setRate(rate);
        rule.setId("1");
        rule.setName("spain and repeats");
        try {
            rule.setRestrictions(new ObjectMapper().readValue(ruleString, Map.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ApplicableRate applicableRate = new ApplicableRate();
        boolean match = true;
        for (Map.Entry<String, Object> ruleEntry: rule.getRestrictions().entrySet()) {
            if (ruleEntry.getValue() instanceof ArrayList<?>) {
                for(Map<String, Object> entry: (ArrayList<Map<String, Object>>)ruleEntry.getValue()) {
                    if (!rulesMatch(entry, calculRequest)) {
                        match = false;
                    }
                }
            } else {
                String value = ((Map<String, String>) ruleEntry.getValue()).entrySet().stream().findFirst().orElseThrow().getValue();
                if (!match(ruleEntry.getKey(), value, calculRequest)) {
                    match = false;
                }
            }

        }
        if (match) {
            applicableRate.setFees(rule.getRate().getPercent());
            applicableRate.setReason(rule.getName());
        } else {
            applicableRate.setFees(10L);
        }
        return applicableRate;
    }

    private boolean rulesMatch(Map<String, Object> rules, CalculRequest calculRequest) {
        if (rules.containsKey("@or")) {
            return orOperatorMatches(rules, calculRequest);
        } else if (rules.containsKey("@and")) {
            return andOperatorMatches(rules, calculRequest);
        } else {
            return leafNodeMatches(rules, calculRequest);
        }
    }

    private boolean orOperatorMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        ArrayList<Map<String, Object>> orConditions = (ArrayList<Map<String, Object>>) rules.get("@or");
        for (Map<String, Object> orCondition: orConditions) {
            if (rulesMatch(orCondition, calculRequest)) {
                return true;
            }
        }
        return false;
    }

    private boolean andOperatorMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        ArrayList<Map<String, Object>> andConditions = (ArrayList<Map<String, Object>>)rules.get("@and");
        for(Map<String, Object> andCondition: andConditions) {
            if (!rulesMatch(andCondition, calculRequest)) {
                return false;
            }
        }
        return true;
    }

    private boolean leafNodeMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        for (Map.Entry<String, Object> condition : rules.entrySet()) {
            Map.Entry<String, String> entry = ((Map<String, String>) condition.getValue()).entrySet().stream().findFirst().orElseThrow();
            return match(condition.getKey(), entry.getKey(), calculRequest);
        }
        return false;
    }

    private boolean match(String key, String value, CalculRequest calculRequest) {
        switch (key) {
            case "@mission.duration" -> {
                return computeMissionDurationRule(value, calculRequest);
            }
            case "@commercialRelationship.duration" -> {
                return computeCommercialRelationRule(value, calculRequest);
            }
            case "@client.location" -> {
                return computeClientLocationRule(value, calculRequest);
            }
            case "@freelancer.location" -> {
                return computeFreelanceLocationRule(value, calculRequest);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean computeMissionDurationRule(String value, CalculRequest calculRequest) {
        /*long durationInMonths = ChronoUnit.MONTHS.between(LocalDateTime.now(), calculRequest.getCommercialRelationship().getLastMission());
        long value = Long.parseLong(entry.getValue().substring(0, 1));
        return computeValueComparison(entry.getKey(), durationInMonths, value);*/
        return true;
    }

    private boolean computeCommercialRelationRule(String value, CalculRequest calculRequest) {
        /*long durationInMonths = ChronoUnit.MONTHS.between(LocalDateTime.now(), calculRequest.getCommercialRelationship().getFirstMission());
        long value = Long.parseLong(entry.getValue().substring(0, 1));
        return computeValueComparison(entry.getKey(), durationInMonths, value);*/
        return true;
    }

    private boolean computeClientLocationRule(String value, CalculRequest calculRequest) {
        return value.equals(calculRequest.getClient().getIp());
    }

    private boolean computeFreelanceLocationRule(String value, CalculRequest calculRequest) {
        return value.equals(calculRequest.getFreelancer().getIp());
    }

    private boolean computeValueComparison(String operator, long valueToCompare, long value) {
        switch (operator) {
            case "gt" -> {
                return valueToCompare > value;
            }
            case "eq" -> {
                return valueToCompare == value;
            }
            case "lt" -> {
                return valueToCompare < value;
            }
            case "ge" -> {
                return valueToCompare >= value;
            }
            case "le" -> {
                return valueToCompare <= value;
            }
            default -> {
                return false;
            }
        }
    }
}

