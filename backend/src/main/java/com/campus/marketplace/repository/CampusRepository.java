package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, Long> {
    boolean existsByCode(String code);
    Optional<Campus> findByCode(String code);
}
