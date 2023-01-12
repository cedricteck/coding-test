package com.malt.codingtest.controller;

import com.malt.codingtest.dto.RuleDto;
import com.malt.codingtest.dto.ApplicableRateDto;
import com.malt.codingtest.dto.CalculRequestDto;
import com.malt.codingtest.service.RateRuleEngineService;
import com.malt.codingtest.service.RuleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule")
public class RuleController {

    @Autowired
    private RateRuleEngineService rateRuleEngineService;

    @Autowired
    private RuleService ruleService;
    @PostMapping
    public ApplicableRateDto findApplicableRate(@RequestBody CalculRequestDto calculRequestDto) {
        return rateRuleEngineService.findApplicableRate(calculRequestDto);
    }

    @PostMapping("/add")
    public RuleDto addRule(@RequestBody RuleDto rule) {
        return ruleService.addRule(rule);
    }
}
