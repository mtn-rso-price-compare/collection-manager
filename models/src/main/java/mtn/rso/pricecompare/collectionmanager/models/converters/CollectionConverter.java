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
        // If collection is locked, do not set amounts
        if(collectionItemEntities != null) {
            dto.setItemList(collectionItemEntities.stream()
                    .map(cie -> CollectionItemConverter.toDto(cie, !entity.getLocked()))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static CollectionEntity toEntity(Collection dto) {

        CollectionEntity entity = new CollectionEntity();
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getCollectionName());
        entity.setLocked(false);
        return entity;
    }

    public static void completeEntity(CollectionEntity partialEntity, CollectionEntity fullEntity) {

        if(partialEntity.getUserId() == null)
            partialEntity.setUserId(fullEntity.getUserId());
        if(partialEntity.getName() == null)
            partialEntity.setName(fullEntity.getName());
        if(partialEntity.getLocked() == null)
            partialEntity.setLocked(fullEntity.getLocked());
    }

}
