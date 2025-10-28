package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.entity.GoodsTag;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.exception.BusinessException;
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
}
