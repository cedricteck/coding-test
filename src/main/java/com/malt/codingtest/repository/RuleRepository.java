package com.malt.codingtest.repository;

import com.malt.codingtest.model.Rule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<Rule, String> {

}
