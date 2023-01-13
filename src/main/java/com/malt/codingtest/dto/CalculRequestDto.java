package com.malt.codingtest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculRequestDto {

    private PersonDto client;

    private PersonDto freelancer;

    private CommercialRelationshipDto commercialRelationship;
}
