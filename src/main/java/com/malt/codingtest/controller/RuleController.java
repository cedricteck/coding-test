package com.malt.codingtest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.codingtest.model.ApplicableRate;
import com.malt.codingtest.model.CalculRequest;
import com.malt.codingtest.service.ComputeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule")
public class RuleController {

    @Autowired
    private ComputeRateService computeRateService;
    @PostMapping
    public ApplicableRate sayHello(@RequestBody CalculRequest calculRequest) {
        return computeRateService.findApplicableRate(calculRequest);
    }
}
