package com.malt.codingtest.service;

import com.malt.codingtest.dto.RuleDto;
import com.malt.codingtest.mapper.RuleMapper;
import com.malt.codingtest.model.Rule;
import com.malt.codingtest.repository.RuleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author cmarechal
 * @created 12/01/2023 - 10:53
 * @project coding-test
 */
@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleMapper ruleMapper;

    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }

    public RuleDto addRule(RuleDto ruleDto) {
        return ruleMapper.toDto(ruleRepository.save(ruleMapper.toEntity(ruleDto)));
    }

    public Map<String, Object> convertRestrictionStringToMap(String restrictions) {
        return ruleMapper.convertJsonStringToMap(restrictions);
    }
}
