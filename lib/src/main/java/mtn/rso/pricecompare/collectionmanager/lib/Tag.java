package mtn.rso.pricecompare.collectionmanager.lib;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class Tag {

    @Schema(example = "1")
    private Integer tagId;
    @Schema(example = "Meso in mesni izdelki")
    private String tagName;
    private List<ItemDTO> itemList;

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public List<ItemDTO> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemDTO> itemList) {
        this.itemList = itemList;
    }
}
