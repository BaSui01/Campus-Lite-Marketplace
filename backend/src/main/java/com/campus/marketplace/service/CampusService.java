package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;

import java.util.List;

public interface CampusService {
    List<Campus> listAll();
    Campus create(String code, String name);
    Campus update(Long id, String name, CampusStatus status);
    void delete(Long id);

    CampusMigrationValidationResponse validateUserMigration(Long fromCampusId, Long toCampusId);
    int migrateUsers(Long fromCampusId, Long toCampusId);
}
