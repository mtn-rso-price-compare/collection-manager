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
    private List<Item> itemList;

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

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }
}
