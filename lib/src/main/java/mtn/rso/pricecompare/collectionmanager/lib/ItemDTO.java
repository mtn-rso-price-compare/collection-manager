package mtn.rso.pricecompare.collectionmanager.lib;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class ItemDTO {

    @Schema(example = "1")
    private Integer itemId;
    @Schema(example = "piščančje prsi")
    private String itemName;
    @Schema(example = "2")
    private Integer amount;
    private List<Price> priceList;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public List<Price> getPriceList() {
        return priceList;
    }

    public void setPriceList(List<Price> priceList) {
        this.priceList = priceList;
    }

}
