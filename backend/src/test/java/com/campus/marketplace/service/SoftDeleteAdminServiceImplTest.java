package com.campus.marketplace.service;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.impl.SoftDeleteAdminServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("软删除治理服务实现测试")
class SoftDeleteAdminServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SoftDeleteAdminServiceImpl service;

    @Test
    @DisplayName("列出支持的实体标识保持固定顺序")
    void listTargets_returnsOrderedKeys() {
        List<String> targets = service.listTargets();
        assertThat(targets).containsExactlyInAnyOrder(
                "post", "goods", "tag", "reply", "favorite", "follow", "campus", "category"
        );
    }

    @Test
    @DisplayName("恢复软删除记录成功（布尔标记）")
    void restore_success_booleanFlag() {
        Query nativeQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(Boolean.TRUE);

        when(entityManager.createQuery(startsWith("update"))).thenReturn(updateQuery);
        when(updateQuery.setParameter(eq("id"), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        service.restore("post", 1L);

        verify(entityManager).createQuery(startsWith("update Post"));
        verify(updateQuery).setParameter("id", 1L);
    }

    @Test
    @DisplayName("恢复软删除记录成功（数字标记）")
    void restore_success_numericFlag() {
        Query nativeQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(1);

        when(entityManager.createQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(eq("id"), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        service.restore("goods", 10L);

        verify(updateQuery).setParameter("id", 10L);
    }

    @Test
    @DisplayName("恢复软删除记录成功（字符串标记）")
    void restore_success_stringFlag() {
        Query nativeQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("1");

        when(entityManager.createQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(eq("id"), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        service.restore("tag", 5L);

        verify(updateQuery).executeUpdate();
    }

    @Test
    @DisplayName("恢复失败：记录未被软删除")
    void restore_notDeleted_throws() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(false);

        assertThatThrownBy(() -> service.restore("reply", 7L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode())
                .hasMessageContaining("未被软删除");

        verify(entityManager, never()).createQuery(startsWith("update"));
    }

    @Test
    @DisplayName("恢复失败：记录不存在")
    void restore_notFound_throws() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThatThrownBy(() -> service.restore("favorite", 9L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("恢复失败：未知标记类型")
    void restore_unknownFlagType_throws() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(new Object());

        assertThatThrownBy(() -> service.restore("follow", 3L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode())
                .hasMessageContaining("无法识别");
    }

    @Test
    @DisplayName("恢复失败：执行更新返回 0")
    void restore_updateZero_throws() {
        Query nativeQuery = mock(Query.class);
        Query updateQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(Boolean.TRUE);

        when(entityManager.createQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(eq("id"), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(0);

        assertThatThrownBy(() -> service.restore("campus", 6L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode())
                .hasMessageContaining("恢复失败");
    }

    @Test
    @DisplayName("彻底删除成功")
    void purge_success() {
        Query nativeQuery = mock(Query.class);
        Query deleteQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(Boolean.TRUE);

        when(entityManager.createQuery(startsWith("delete"))).thenReturn(deleteQuery);
        when(deleteQuery.setParameter(eq("id"), any())).thenReturn(deleteQuery);
        when(deleteQuery.executeUpdate()).thenReturn(1);

        service.purge("category", 11L);

        verify(deleteQuery).executeUpdate();
    }

    @Test
    @DisplayName("彻底删除失败：记录不存在")
    void purge_notFound_throws() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThatThrownBy(() -> service.purge("post", 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOT_FOUND.getCode());

        verify(entityManager, never()).createQuery(startsWith("delete"));
    }

    @Test
    @DisplayName("彻底删除失败：执行删除返回 0")
    void purge_deleteZero_throws() {
        Query nativeQuery = mock(Query.class);
        Query deleteQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(Boolean.TRUE);

        when(entityManager.createQuery(anyString())).thenReturn(deleteQuery);
        when(deleteQuery.setParameter(eq("id"), any())).thenReturn(deleteQuery);
        when(deleteQuery.executeUpdate()).thenReturn(0);

        assertThatThrownBy(() -> service.purge("goods", 4L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode())
                .hasMessageContaining("彻底删除失败");
    }

    @Test
    @DisplayName("解析实体标识失败：为空")
    void resolve_nullKey_throws() {
        assertThatThrownBy(() -> service.restore(null, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PARAM_ERROR.getCode());
    }

    @Test
    @DisplayName("解析实体标识失败：不支持的类型")
    void resolve_unknownKey_throws() {
        assertThatThrownBy(() -> service.restore("unknown", 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PARAM_ERROR.getCode());
    }
}
