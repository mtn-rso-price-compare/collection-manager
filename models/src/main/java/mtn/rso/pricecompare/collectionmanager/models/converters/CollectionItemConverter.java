package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.ItemDTO;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;

public class CollectionItemConverter {

    public static ItemDTO toDto(CollectionItemEntity collectionItemEntity, Boolean setAmount) {

        ItemDTO item = new ItemDTO();
        item.setItemId(collectionItemEntity.getItemId());
        if(setAmount)
            item.setAmount(collectionItemEntity.getAmount());
        return item;
    }

    public static CollectionItemEntity toEntity(Integer collectionId, Integer itemId, Integer amount) {

        CollectionItemEntity entity = new CollectionItemEntity();
        entity.setCollectionId(collectionId);
        entity.setItemId(itemId);
        entity.setAmount(amount);
        return entity;
    }
}
