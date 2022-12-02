package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.Item;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;

public class TagItemConverter {

    public static Item toDto(TagItemEntity tagItemEntity) {

        Item item = new Item();
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
