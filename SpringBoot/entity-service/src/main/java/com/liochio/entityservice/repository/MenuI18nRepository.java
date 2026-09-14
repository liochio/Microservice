package com.liochio.entityservice.repository;

import com.liochio.entityservice.entity.MenuI18nEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuI18nRepository extends JpaRepository<MenuI18nEntity, Long> {

    List<MenuI18nEntity> findByLang(String lang);

    Optional<MenuI18nEntity> findByMenuCodeAndLang(String menuCode, String lang);
}
