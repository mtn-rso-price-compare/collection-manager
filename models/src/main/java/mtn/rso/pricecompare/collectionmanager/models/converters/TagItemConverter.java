package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.ItemDTO;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;

public class TagItemConverter {

    public static ItemDTO toDto(TagItemEntity tagItemEntity) {

        ItemDTO item = new ItemDTO();
        item.setItemId(tagItemEntity.getItemId());
        return item;
    }

    public static TagItemEntity toEntity(Integer tagId, Integer itemId) {
        
        TagItemEntity entity = new TagItemEntity();
        entity.setTagId(tagId);
        entity.setItemId(itemId);
        return entity;
    }
}
