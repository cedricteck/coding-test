package com.malt.codingtest.controller;

import com.malt.codingtest.model.ApplicableRate;
import com.malt.codingtest.model.CalculRequest;
import com.malt.codingtest.model.Rule;
import com.malt.codingtest.repository.RuleRepository;
import com.malt.codingtest.service.RateRuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule")
public class RuleController {

    @Autowired
    private RateRuleEngineService rateRuleEngineService;

    @Autowired
    private RuleRepository ruleRepository;
    @PostMapping
    public ApplicableRate findApplicableRate(@RequestBody CalculRequest calculRequest) {
        return rateRuleEngineService.findApplicableRate(calculRequest);
    }

    @PostMapping("/add")
    public void addRule(@RequestBody Rule rule) {
        ruleRepository.addRule(rule);
    }
}
