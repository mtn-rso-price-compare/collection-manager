package mtn.rso.pricecompare.collectionmanager.models.converters;

import mtn.rso.pricecompare.collectionmanager.lib.Collection;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionConverter {

    public static Collection toDto(CollectionEntity entity, List<CollectionItemEntity> collectionItemEntities) {

        Collection dto = new Collection();
        dto.setCollectionId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setCollectionName(entity.getName());
        dto.setItemList(collectionItemEntities.stream()
                .map(CollectionItemConverter::toDto).collect(Collectors.toList()));
        return dto;
    }

    public static CollectionEntity toEntity(Collection dto) {

        CollectionEntity entity = new CollectionEntity();
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getCollectionName());
        return entity;
    }

}
