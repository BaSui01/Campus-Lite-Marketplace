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
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
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
        org.mockito.Mockito.verify(tagRepository).save(org.mockito.ArgumentMatchers.argThat(tag ->
                tag.getName().equals("数码") && tag.getEnabled()));
    }

    @Test
    @DisplayName("合并标签应重定向商品绑定")
    void mergeTag_shouldReassignBindings() {
        Tag source = Tag.builder().id(1L).name("old").enabled(true).build();
        Tag target = Tag.builder().id(2L).name("new").enabled(true).build();

        GoodsTag binding1 = GoodsTag.builder().id(10L).goodsId(100L).tagId(1L).build();
        GoodsTag binding2 = GoodsTag.builder().id(11L).goodsId(200L).tagId(1L).build();

        org.mockito.Mockito.when(tagRepository.findById(1L)).thenReturn(java.util.Optional.of(source));
        org.mockito.Mockito.when(tagRepository.findById(2L)).thenReturn(java.util.Optional.of(target));
        org.mockito.Mockito.when(goodsTagRepository.findByTagId(1L)).thenReturn(java.util.List.of(binding1, binding2));
        org.mockito.Mockito.when(goodsTagRepository.findByGoodsIdAndTagId(100L, 2L))
                .thenReturn(java.util.Optional.of(GoodsTag.builder().id(12L).goodsId(100L).tagId(2L).build()));
        org.mockito.Mockito.when(goodsTagRepository.findByGoodsIdAndTagId(200L, 2L))
                .thenReturn(java.util.Optional.empty());

        tagService.mergeTag(new MergeTagRequest(1L, 2L));

        org.mockito.Mockito.verify(goodsTagRepository).deleteById(10L);
        org.mockito.Mockito.verify(goodsTagRepository).save(org.mockito.ArgumentMatchers.argThat(gt ->
                gt.getGoodsId().equals(200L) && gt.getTagId().equals(2L)));
        org.mockito.Mockito.verify(tagRepository).delete(source);
    }

    @Test
    @DisplayName("合并标签时源标签不存在应抛出异常")
    void mergeTag_sourceMissing_shouldThrow() {
        org.mockito.Mockito.when(tagRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class,
                () -> tagService.mergeTag(new MergeTagRequest(1L, 2L)));
    }
}
