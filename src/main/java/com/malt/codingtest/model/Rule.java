package com.malt.codingtest.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Rule {

    private String id;

    private String name;

    private Rate rate;

    private Map<String, Object> restrictions;
}
