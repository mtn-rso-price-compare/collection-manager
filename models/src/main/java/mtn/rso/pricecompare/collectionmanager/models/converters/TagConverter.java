package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.Tag;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;

import java.util.List;
import java.util.stream.Collectors;

public class TagConverter {

    public static Tag toDto(TagEntity entity, List<TagItemEntity> tagItemEntities) {

        Tag dto = new Tag();
        dto.setTagId(entity.getId());
        dto.setTagName(entity.getName());
        if(tagItemEntities != null)
            dto.setItemList(tagItemEntities.stream()
                .map(TagItemConverter::toDto).collect(Collectors.toList()));
        return dto;
    }

    public static TagEntity toEntity(Tag dto) {

        TagEntity entity = new TagEntity();
        entity.setName(dto.getTagName());
        return entity;
    }

    public static void completeTag(TagEntity partialEntity, TagEntity fullEntity) {
        if(partialEntity.getName() == null)
            partialEntity.setName(fullEntity.getName());
    }

}
