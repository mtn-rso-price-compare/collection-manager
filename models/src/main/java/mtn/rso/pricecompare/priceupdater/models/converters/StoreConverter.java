package mtn.rso.pricecompare.priceupdater.models.converters;

import mtn.rso.pricecompare.priceupdater.lib.Store;
import mtn.rso.pricecompare.priceupdater.models.entities.PriceEntity;
import mtn.rso.pricecompare.priceupdater.models.entities.StoreEntity;

import java.util.List;
import java.util.stream.Collectors;

public class StoreConverter {

    public static Store toDto(StoreEntity entity, boolean setPrices) {

        Store dto = new Store();
        dto.setStoreId(entity.getId());
        dto.setStoreName(entity.getName());
        dto.setUrl(entity.getUrl());
        if(setPrices)
            dto.setPriceList(entity.getPriceEntityList().stream()
                .map(pe -> PriceConverter.toDto(pe, true, false))
                .collect(Collectors.toList()));

        return dto;
    }

    public static StoreEntity toEntity(Store dto, List<PriceEntity> priceEntityList) {

        StoreEntity entity = new StoreEntity();
        entity.setName(dto.getStoreName());
        entity.setUrl(dto.getUrl());
        entity.setPriceEntityList(priceEntityList);

        return entity;
    }

}
