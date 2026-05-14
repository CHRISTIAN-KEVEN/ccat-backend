package com.ccat.api.repository;

import com.ccat.api.model.entity.AppConfig;
import com.ccat.api.model.enums.ConfigGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    Optional<AppConfig> findByStrKey(String strKey);
    List<AppConfig> findByEmGroup(ConfigGroup emGroup);
    boolean existsByStrKey(String strKey);
}
