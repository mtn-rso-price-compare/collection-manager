package mtn.rso.pricecompare.priceupdater.models.converters;

import mtn.rso.pricecompare.priceupdater.lib.Request;
import mtn.rso.pricecompare.priceupdater.models.entities.RequestEntity;

public class RequestConverter {

    public static Request toDto(RequestEntity entity) {

        Request dto = new Request();
        dto.setRequestId(entity.getId());
        dto.setStatus(statusToString(entity.getStatus()));
        dto.setLastUpdated(entity.getLastUpdated());

        return dto;
    }

    public static RequestEntity toEntity(Request dto) {

        RequestEntity entity = new RequestEntity();
        entity.setStatus(statusToInteger(dto.getStatus()));
        entity.setLastUpdated(dto.getLastUpdated());

        return entity;
    }

    public static String statusToString(Integer status) {
        return switch (status) {
            case 0 -> "SUCCESS";
            case 1 -> "PROCESSING";
            case 2 -> "FAILURE";
            default -> "UNKNOWN";
        };
    }

    public static Integer statusToInteger(String status) {
        return switch (status) {
            case "SUCCESS" -> 0;
            case "PROCESSING" -> 1;
            case "FAILURE" -> 2;
            default -> -1;
        };
    }

}
