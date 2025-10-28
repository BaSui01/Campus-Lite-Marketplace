package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.CampusServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("校区服务实现测试")
class CampusServiceImplTest {

    @Mock private CampusRepository campusRepository;
    @Mock private GoodsRepository goodsRepository;
    @Mock private PostRepository postRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CampusServiceImpl campusService;

    @Test
    @DisplayName("创建校区成功保存激活状态")
    void createCampus_success() {
        when(campusRepository.existsByCode("A001")).thenReturn(false);
        when(campusRepository.save(any(Campus.class))).thenAnswer(inv -> {
            Campus campus = inv.getArgument(0);
            campus.setId(10L);
            return campus;
        });

        Campus saved = campusService.create("A001", "主校区");

        assertThat(saved.getStatus()).isEqualTo(CampusStatus.ACTIVE);
        assertThat(saved.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("创建校区编码重复抛异常")
    void createCampus_duplicateCode() {
        when(campusRepository.existsByCode("A001")).thenReturn(true);

        assertThatThrownBy(() -> campusService.create("A001", "主校区"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_PARAMETER.getCode());
    }

    @Test
    @DisplayName("更新校区成功写回仓库")
    void updateCampus_success() {
        Campus campus = Campus.builder().name("旧").status(CampusStatus.ACTIVE).build();
        campus.setId(5L);
        when(campusRepository.findById(5L)).thenReturn(Optional.of(campus));
        when(campusRepository.save(campus)).thenReturn(campus);

        Campus updated = campusService.update(5L, "新名称", CampusStatus.INACTIVE);

        assertThat(updated.getName()).isEqualTo("新名称");
        assertThat(updated.getStatus()).isEqualTo(CampusStatus.INACTIVE);
    }

    @Test
    @DisplayName("删除校区存在关联数据时拒绝")
    void deleteCampus_withRelations() {
        Campus campus = new Campus();
        campus.setId(8L);
        when(campusRepository.findById(8L)).thenReturn(Optional.of(campus));
        when(goodsRepository.countByCampusId(8L)).thenReturn(1L);

        assertThatThrownBy(() -> campusService.delete(8L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
        verify(campusRepository, never()).delete(any());
    }

    @Test
    @DisplayName("删除校区无关联数据时成功")
    void deleteCampus_success() {
        Campus campus = new Campus();
        campus.setId(9L);
        when(campusRepository.findById(9L)).thenReturn(Optional.of(campus));
        when(goodsRepository.countByCampusId(9L)).thenReturn(0L);
        when(postRepository.countByCampusId(9L)).thenReturn(0L);
        when(orderRepository.countByCampusId(9L)).thenReturn(0L);
        when(userRepository.countByCampusId(9L)).thenReturn(0L);

        campusService.delete(9L);

        verify(campusRepository).delete(campus);
    }

    @Test
    @DisplayName("校区迁移参数非法抛异常")
    void validateMigration_invalidParams() {
        assertThatThrownBy(() -> campusService.validateUserMigration(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_PARAMETER.getCode());
    }

    @Test
    @DisplayName("目标校区未启用返回禁止迁移")
    void validateMigration_targetInactive() {
        Campus from = buildCampus(1L, CampusStatus.ACTIVE);
        Campus to = buildCampus(2L, CampusStatus.INACTIVE);
        when(campusRepository.findById(1L)).thenReturn(Optional.of(from));
        when(campusRepository.findById(2L)).thenReturn(Optional.of(to));

        CampusMigrationValidationResponse resp = campusService.validateUserMigration(1L, 2L);

        assertThat(resp.isAllowed()).isFalse();
        assertThat(resp.getReason()).isEqualTo("目标校区未启用");
    }

    @Test
    @DisplayName("校区迁移校验返回关联数据统计")
    void validateMigration_counts() {
        Campus from = buildCampus(3L, CampusStatus.ACTIVE);
        Campus to = buildCampus(4L, CampusStatus.ACTIVE);
        when(campusRepository.findById(3L)).thenReturn(Optional.of(from));
        when(campusRepository.findById(4L)).thenReturn(Optional.of(to));
        when(goodsRepository.countByCampusId(3L)).thenReturn(2L);
        when(postRepository.countByCampusId(3L)).thenReturn(1L);
        when(orderRepository.countByCampusId(3L)).thenReturn(0L);
        when(userRepository.countByCampusId(3L)).thenReturn(5L);

        CampusMigrationValidationResponse resp = campusService.validateUserMigration(3L, 4L);

        assertThat(resp.isAllowed()).isFalse();
        assertThat(resp.getGoodsCount()).isEqualTo(2L);
        assertThat(resp.getUserCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("用户迁移成功执行仓库更新")
    void migrateUsers_success() {
        Campus from = buildCampus(5L, CampusStatus.ACTIVE);
        Campus to = buildCampus(6L, CampusStatus.ACTIVE);
        when(campusRepository.findById(5L)).thenReturn(Optional.of(from));
        when(campusRepository.findById(6L)).thenReturn(Optional.of(to));
        when(goodsRepository.countByCampusId(5L)).thenReturn(0L);
        when(postRepository.countByCampusId(5L)).thenReturn(0L);
        when(orderRepository.countByCampusId(5L)).thenReturn(0L);
        when(userRepository.countByCampusId(5L)).thenReturn(0L);
        when(userRepository.updateCampusByCampusId(5L, 6L)).thenReturn(12);

        int updated = campusService.migrateUsers(5L, 6L);

        assertThat(updated).isEqualTo(12);
        verify(userRepository).updateCampusByCampusId(5L, 6L);
    }

    private static Campus buildCampus(Long id, CampusStatus status) {
        Campus campus = Campus.builder()
                .code("C" + id)
                .name("校园" + id)
                .status(status)
                .build();
        campus.setId(id);
        return campus;
    }
}
