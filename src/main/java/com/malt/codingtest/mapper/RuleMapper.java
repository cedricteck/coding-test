package com.malt.codingtest.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.codingtest.dto.RateDto;
import com.malt.codingtest.dto.RuleDto;
import com.malt.codingtest.model.Rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import lombok.Setter;

/**
 * @author cmarechal
 * @created 12/01/2023 - 11:28
 * @project coding-test
 */

@Service
public class RuleMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TypeReference<Map<String,Object>> typeRef = new TypeReference<>() {};

    public Rule toEntity(RuleDto dto) {
        Rule rule = new Rule();
        rule.setId(dto.getId());
        rule.setName(dto.getName());
        rule.setRate(dto.getRate().getPercent());
        try {
            rule.setRestrictions(objectMapper.writeValueAsString(dto.getRestrictions()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return rule;
    }

    public RuleDto toDto(Rule rule) {
        RuleDto ruleDto = new RuleDto();
        ruleDto.setId(rule.getId());
        ruleDto.setName(rule.getName());
        RateDto rateDto = new RateDto();
        rateDto.setPercent(rule.getRate());
        ruleDto.setRate(rateDto);
        ruleDto.setRestrictions(convertJsonStringToMap(rule.getRestrictions()));
        return ruleDto;
    }

    public Map<String, Object> convertJsonStringToMap(String json) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
