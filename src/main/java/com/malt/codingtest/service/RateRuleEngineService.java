package com.malt.codingtest.service;

import com.malt.codingtest.model.ApplicableRate;
import com.malt.codingtest.model.CalculRequest;
import com.malt.codingtest.model.Rule;
import com.malt.codingtest.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RateRuleEngineService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private LocalisationService localisationService;
    
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String AND_OPERATOR = "@and";

    private static final String OR_OPERATOR = "@or";

    public ApplicableRate findApplicableRate(CalculRequest calculRequest) {
        ApplicableRate applicableRate = new ApplicableRate();
        applicableRate.setFees(10L);
       for (Rule rule: ruleRepository.getRules()) {
           Map<String, Object> map = new HashMap<>();
           List<Map<String, Object>> andConditions = new ArrayList<>();
           for (Map.Entry<String, Object> entry : rule.getRestrictions().entrySet()) {
               andConditions.add(Map.ofEntries(entry));
           }
           map.put(AND_OPERATOR, andConditions);
           if (rulesMatch(map, calculRequest)) {
               applicableRate.setFees(rule.getRate().getPercent());
               applicableRate.setReason(rule.getName());
               return applicableRate;
           }
       }
       return applicableRate;
    }

    private boolean rulesMatch(Map<String, Object> rules, CalculRequest calculRequest) {
        if (rules.containsKey(OR_OPERATOR)) {
            return orOperatorMatches(rules, calculRequest);
        } else if (rules.containsKey(AND_OPERATOR)) {
            return andOperatorMatches(rules, calculRequest);
        } else {
            return leafNodeMatches(rules, calculRequest);
        }
    }

    private boolean orOperatorMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        List<Map<String, Object>> orConditions = (List<Map<String, Object>>) rules.get("@or");
        for (Map<String, Object> orCondition: orConditions) {
            if (rulesMatch(orCondition, calculRequest)) {
                return true;
            }
        }
        return false;
    }

    private boolean andOperatorMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        List<Map<String, Object>> andConditions = (List<Map<String, Object>>)rules.get("@and");
        for(Map<String, Object> andCondition: andConditions) {
            if (!rulesMatch(andCondition, calculRequest)) {
                return false;
            }
        }
        return true;
    }

    private boolean leafNodeMatches(Map<String, Object> rules, CalculRequest calculRequest) {
        for (Map.Entry<String, Object> condition : rules.entrySet()) {
            Map.Entry<String, Object> entry = ((Map<String, Object>) condition.getValue()).entrySet().stream().findFirst().orElseThrow();
            return match(condition.getKey(), entry, calculRequest);
        }
        return false;
    }

    private boolean match(String key, Map.Entry<String, Object> value, CalculRequest calculRequest) {
        switch (key) {
            case "@mission.duration" -> {
                return computeMissionDurationRule(value, calculRequest);
            }
            case "@commercialRelationship.duration" -> {
                return computeCommercialRelationRule(value, calculRequest);
            }
            case "@client.location" -> {
                return computeClientLocationRule((String) value.getValue(), calculRequest);
            }
            case "@freelancer.location" -> {
                return computeFreelanceLocationRule((String) value.getValue(), calculRequest);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean computeMissionDurationRule(Map.Entry<String, Object> entryValue, CalculRequest calculRequest) {
        return computeValueComparison(entryValue.getKey(),
                findDuration(calculRequest.getCommercialRelationship().getLastMission()), findDurationFromRuleEntry(entryValue));
    }

    private long findDurationFromRuleEntry(Map.Entry<String, Object> entryValue) {
        String stringValue = (String) entryValue.getValue();
        return Long.parseLong(stringValue.substring(0, stringValue.length() - 6));
    }

    private long findDuration(String calculRequestDate) {
        return ChronoUnit.MONTHS.between(LocalDate.parse(calculRequestDate.substring(0, 10),
                dateTimeFormatter).atStartOfDay(), LocalDateTime.now());
    }

    private boolean computeCommercialRelationRule(Map.Entry<String, Object> entryValue, CalculRequest calculRequest) {
        return computeValueComparison(entryValue.getKey(),
                findDuration(calculRequest.getCommercialRelationship().getFirstMission()), findDurationFromRuleEntry(entryValue));
    }

    private boolean computeClientLocationRule(String value, CalculRequest calculRequest) {
        return value.equals(localisationService.localizeFromIp(calculRequest.getClient().getIp()).getCountry_code());
    }

    private boolean computeFreelanceLocationRule(String value, CalculRequest calculRequest) {
        return value.equals(localisationService.localizeFromIp(calculRequest.getFreelancer().getIp()).getCountry_code());
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

