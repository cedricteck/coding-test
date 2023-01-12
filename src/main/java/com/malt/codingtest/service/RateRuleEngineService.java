package com.malt.codingtest.service;

import com.malt.codingtest.dto.ApplicableRateDto;
import com.malt.codingtest.dto.CalculRequestDto;
import com.malt.codingtest.model.Rule;

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
    private RuleService ruleService;

    @Autowired
    private LocalisationService localisationService;
    
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String AND_OPERATOR = "@and";

    private static final String OR_OPERATOR = "@or";

    private static final long DEFAULT_FEE = 10L;

    /**
     * Find an applicable rate based on the calcul request and the rules in the system
     * @param calculRequestDto calcul request containing client, freelancer and commercial relationship infos
     * @return ApplicableRateDto with fees find in matching rule or default (10)
     */
    public ApplicableRateDto findApplicableRate(CalculRequestDto calculRequestDto) {
        ApplicableRateDto applicableRateDto = new ApplicableRateDto();
        applicableRateDto.setFees(DEFAULT_FEE);
       for (Rule rule: ruleService.findAll()) {
           Map<String, Object> map = new HashMap<>();
           List<Map<String, Object>> andConditions = new ArrayList<>();
           // convert restrictions to a list of "and" conditions
           for (Map.Entry<String, Object> entry :
                   ruleService.convertRestrictionStringToMap(rule.getRestrictions()).entrySet()) {
               andConditions.add(Map.ofEntries(entry));
           }
           map.put(AND_OPERATOR, andConditions);
           if (rulesMatch(map, calculRequestDto)) {
               applicableRateDto.setFees(rule.getRate());
               applicableRateDto.setReason(rule.getName());
               return applicableRateDto;
           }
       }
       return applicableRateDto;
    }

    private boolean rulesMatch(Map<String, Object> rules, CalculRequestDto calculRequestDto) {
        // recursively go through all the "or" and "and" operator until finding the leaf operators
        if (rules.containsKey(OR_OPERATOR)) {
            return orOperatorMatches(rules, calculRequestDto);
        } else if (rules.containsKey(AND_OPERATOR)) {
            return andOperatorMatches(rules, calculRequestDto);
        } else {
            return leafOperatorMatches(rules, calculRequestDto);
        }
    }

    private boolean orOperatorMatches(Map<String, Object> rules, CalculRequestDto calculRequestDto) {
        List<Map<String, Object>> orConditions = (List<Map<String, Object>>) rules.get(OR_OPERATOR);
        for (Map<String, Object> orCondition: orConditions) {
            // if one of the conditions matches, stop there and return true ("or" operator)
            if (rulesMatch(orCondition, calculRequestDto)) {
                return true;
            }
        }
        return false;
    }

    private boolean andOperatorMatches(Map<String, Object> rules, CalculRequestDto calculRequestDto) {
        List<Map<String, Object>> andConditions = (List<Map<String, Object>>)rules.get(AND_OPERATOR);
        // if one of the conditions doesn't match, stop there and return false ("and" operator)
        for(Map<String, Object> andCondition: andConditions) {
            if (!rulesMatch(andCondition, calculRequestDto)) {
                return false;
            }
        }
        return true;
    }

    private boolean leafOperatorMatches(Map<String, Object> rules, CalculRequestDto calculRequestDto) {
        for (Map.Entry<String, Object> condition : rules.entrySet()) {
            Map.Entry<String, Object> entry = ((Map<String, Object>) condition.getValue()).entrySet().stream().findFirst().orElseThrow();
            return match(condition.getKey(), entry, calculRequestDto);
        }
        return false;
    }

    private boolean match(String key, Map.Entry<String, Object> value, CalculRequestDto calculRequestDto) {
        switch (key) {
            case "@mission.duration" -> {
                return computeMissionDurationRule(value, calculRequestDto);
            }
            case "@commercialRelationship.duration" -> {
                return computeCommercialRelationRule(value, calculRequestDto);
            }
            case "@client.location" -> {
                return computeClientLocationRule((String) value.getValue(), calculRequestDto);
            }
            case "@freelancer.location" -> {
                return computeFreelanceLocationRule((String) value.getValue(), calculRequestDto);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean computeMissionDurationRule(Map.Entry<String, Object> entryValue, CalculRequestDto calculRequestDto) {
        return computeValueComparison(entryValue.getKey(),
                findDuration(calculRequestDto.getCommercialRelationshipDto().getLastMission()), findDurationFromRuleEntry(entryValue));
    }

    private long findDurationFromRuleEntry(Map.Entry<String, Object> entryValue) {
        String stringValue = (String) entryValue.getValue();
        return Long.parseLong(stringValue.substring(0, stringValue.length() - 6));
    }

    private long findDuration(String calculRequestDate) {
        return ChronoUnit.MONTHS.between(LocalDate.parse(calculRequestDate.substring(0, 10),
                dateTimeFormatter).atStartOfDay(), LocalDateTime.now());
    }

    private boolean computeCommercialRelationRule(Map.Entry<String, Object> entryValue, CalculRequestDto calculRequestDto) {
        return computeValueComparison(entryValue.getKey(),
                findDuration(calculRequestDto.getCommercialRelationshipDto().getFirstMission()), findDurationFromRuleEntry(entryValue));
    }

    private boolean computeClientLocationRule(String value, CalculRequestDto calculRequestDto) {
        return value.equals(localisationService.localizeFromIp(calculRequestDto.getClient().getIp()).getCountry_code());
    }

    private boolean computeFreelanceLocationRule(String value, CalculRequestDto calculRequestDto) {
        return value.equals(localisationService.localizeFromIp(calculRequestDto.getFreelancer().getIp()).getCountry_code());
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

