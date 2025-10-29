package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.entity.GoodsTag;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.TagRepository;
import com.campus.marketplace.service.impl.TagServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("标签服务测试")
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GoodsTagRepository goodsTagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    @DisplayName("创建标签成功")
    void createTag_success() {
        tagService.createTag(new CreateTagRequest("  数码  ", "电子类", true));
        verify(tagRepository).save(argThat(tag ->
                tag.getName().equals("数码") && tag.getEnabled()));
    }

    @Test
    @DisplayName("创建重复标签抛出业务异常")
    void createTag_duplicate_shouldThrow() {
        Tag existed = Tag.builder().name("数码").enabled(true).build();
        existed.setId(5L);
        when(tagRepository.findByNameIgnoreCase("数码")).thenReturn(Optional.of(existed));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tagService.createTag(new CreateTagRequest("数码", "描述", true)));

        assertEquals(ErrorCode.DUPLICATE_RESOURCE, exception.getErrorCode());
        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新标签成功同步描述与启用状态")
    void updateTag_success() {
        Tag existed = Tag.builder()
                .name("旧名称")
                .description("旧描述")
                .enabled(true)
                .build();
        existed.setId(10L);
        when(tagRepository.findById(10L)).thenReturn(Optional.of(existed));
        when(tagRepository.findByNameIgnoreCase("新名称")).thenReturn(Optional.of(existed));

        tagService.updateTag(10L, new UpdateTagRequest(" 新名称 ", "新描述", false));

        verify(tagRepository).save(existed);
        assertEquals("新名称", existed.getName());
        assertEquals("新描述", existed.getDescription());
        assertEquals(Boolean.FALSE, existed.getEnabled());
    }

    @Test
    @DisplayName("更新标签名称与他人重复抛异常")
    void updateTag_duplicateName_shouldThrow() {
        Tag existed = Tag.builder().name("旧").enabled(true).build();
        existed.setId(11L);
        Tag other = Tag.builder().name("新名称").enabled(true).build();
        other.setId(20L);
        when(tagRepository.findById(11L)).thenReturn(Optional.of(existed));
        when(tagRepository.findByNameIgnoreCase("新名称")).thenReturn(Optional.of(other));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tagService.updateTag(11L, new UpdateTagRequest("新名称", null, null)));

        assertEquals(ErrorCode.DUPLICATE_RESOURCE, exception.getErrorCode());
        verify(tagRepository, never()).save(existed);
    }

    @Test
    @DisplayName("删除标签存在绑定关系抛异常")
    void deleteTag_hasBindings_shouldThrow() {
        Tag existed = Tag.builder().name("测试").enabled(true).build();
        existed.setId(30L);
        when(tagRepository.findById(30L)).thenReturn(Optional.of(existed));
        when(goodsTagRepository.countByTagId(30L)).thenReturn(2L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tagService.deleteTag(30L));

        assertEquals(ErrorCode.OPERATION_FAILED, exception.getErrorCode());
        verify(tagRepository, never()).delete(existed);
    }

    @Test
    @DisplayName("删除标签成功调用仓库删除")
    void deleteTag_success() {
        Tag existed = Tag.builder().name("测试").enabled(true).build();
        existed.setId(31L);
        when(tagRepository.findById(31L)).thenReturn(Optional.of(existed));
        when(goodsTagRepository.countByTagId(31L)).thenReturn(0L);

        tagService.deleteTag(31L);

        verify(tagRepository).delete(existed);
    }

    @Test
    @DisplayName("合并标签应重定向商品绑定")
    void mergeTag_shouldReassignBindings() {
        Tag source = Tag.builder().name("old").enabled(true).build();
        source.setId(1L);
        Tag target = Tag.builder().name("new").enabled(true).build();
        target.setId(2L);

        GoodsTag binding1 = GoodsTag.builder().goodsId(100L).tagId(1L).build();
        binding1.setId(10L);
        GoodsTag binding2 = GoodsTag.builder().goodsId(200L).tagId(1L).build();
        binding2.setId(11L);

        when(tagRepository.findById(1L)).thenReturn(Optional.of(source));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(target));
        when(goodsTagRepository.findByTagId(1L)).thenReturn(List.of(binding1, binding2));
        GoodsTag existed = GoodsTag.builder().goodsId(100L).tagId(2L).build();
        existed.setId(12L);
        when(goodsTagRepository.findByGoodsIdAndTagId(100L, 2L))
                .thenReturn(Optional.of(existed));
        when(goodsTagRepository.findByGoodsIdAndTagId(200L, 2L))
                .thenReturn(Optional.empty());

        tagService.mergeTag(new MergeTagRequest(1L, 2L));

        verify(goodsTagRepository).deleteById(10L);
        verify(goodsTagRepository).save(argThat(gt ->
                gt.getGoodsId().equals(200L) && gt.getTagId().equals(2L)));
        verify(tagRepository).delete(source);
    }

    @Test
    @DisplayName("合并标签时源标签不存在应抛出异常")
    void mergeTag_sourceMissing_shouldThrow() {
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> tagService.mergeTag(new MergeTagRequest(1L, 2L)));
    }

    @Test
    @DisplayName("合并标签时源目标相同抛异常")
    void mergeTag_sameTarget_shouldThrow() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> tagService.mergeTag(new MergeTagRequest(1L, 1L)));

        assertEquals(ErrorCode.OPERATION_FAILED, exception.getErrorCode());
        verify(tagRepository, never()).findById(anyLong());
    }
}
