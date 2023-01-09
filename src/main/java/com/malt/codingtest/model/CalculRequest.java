package com.malt.codingtest.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculRequest {

    private Person client;

    private Person freelancer;

    private CommercialRelationship commercialRelationship;
}
