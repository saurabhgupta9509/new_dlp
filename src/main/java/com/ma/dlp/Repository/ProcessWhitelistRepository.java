package com.ma.dlp.Repository;

import com.ma.dlp.model.ProcessWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessWhitelistRepository extends JpaRepository<ProcessWhitelist, Long> {
    List<ProcessWhitelist> findByProcessName(String processName);
}