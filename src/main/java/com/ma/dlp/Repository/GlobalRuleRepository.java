package com.ma.dlp.Repository;

import com.ma.dlp.model.GlobalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlobalRuleRepository extends JpaRepository<GlobalRule, Long> {
    List<GlobalRule> findAllByOrderByPriorityDesc();
}