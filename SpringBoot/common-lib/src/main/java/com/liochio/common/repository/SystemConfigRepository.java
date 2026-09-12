package com.liochio.common.repository;

import com.liochio.common.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, Long> {

    @Query("SELECT c FROM SystemConfigEntity c WHERE c.envProfile = :envProfile AND c.serviceName IN (:serviceName, 'global') AND c.configKey = :configKey AND c.isActive = true")
    Optional<SystemConfigEntity> findActiveConfig(
            @Param("envProfile") String envProfile,
            @Param("serviceName") String serviceName,
            @Param("configKey") String configKey
    );

    List<SystemConfigEntity> findByEnvProfileAndIsActiveTrue(String envProfile);
}
