package mtn.rso.pricecompare.collectionmanager.lib;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class Collection {

    @Schema(example = "1")
    private Integer collectionId;
    @Schema(example = "1")
    private Integer userId;
    @Schema(example = "Priljubljeni izdelki")
    private String collectionName;
    private List<ItemDTO> itemList;
    private List<Price> priceTotal;

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public List<ItemDTO> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemDTO> itemList) {
        this.itemList = itemList;
    }

    public List<Price> getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(List<Price> priceTotal) {
        this.priceTotal = priceTotal;
    }
}
