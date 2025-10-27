package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    Optional<ExportJob> findByDownloadToken(String token);
}
