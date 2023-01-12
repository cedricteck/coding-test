package com.malt.codingtest.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cmarechal
 * @created 12/01/2023 - 11:27
 * @project coding-test
 */

@Getter
@Setter
public class RuleDto {

    private String id;

    private String name;

    private RateDto rate;

    private Map<String, Object> restrictions;
}
