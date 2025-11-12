package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;

import java.util.List;
/**
 * Campus Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface CampusService {
    List<Campus> listAll();
    Campus getById(Long id);
    Campus create(String code, String name);
    Campus update(Long id, String name, CampusStatus status);
    void delete(Long id);
    int batchDelete(List<Long> ids);

    com.campus.marketplace.common.dto.response.CampusStatisticsResponse getStatistics(Long id);

    CampusMigrationValidationResponse validateUserMigration(Long fromCampusId, Long toCampusId);
    int migrateUsers(Long fromCampusId, Long toCampusId);
}
