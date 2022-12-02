package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.Item;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;

public class CollectionItemConverter {

    public static Item toDto(CollectionItemEntity collectionItemEntity) {

        Item item = new Item();
        item.setItemId(collectionItemEntity.getItemId());
        return item;
    }

    public static CollectionItemEntity toEntity(Integer collectionId, Integer itemId) {

        CollectionItemEntity entity = new CollectionItemEntity();
        entity.setCollectionId(collectionId);
        entity.setItemId(itemId);
        return entity;
    }
}
