package mtn.rso.pricecompare.collectionmanager.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class TagItemKey implements Serializable {

    private Integer tagId;

    private Integer itemId;

    public TagItemKey() {}

    public TagItemKey(Integer tagId, Integer itemId) {
        this.tagId = tagId;
        this.itemId = itemId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Override
    public int hashCode() {
        return (Objects.hash(this.getItemId(), this.getTagId()));
    }

    @Override
    public boolean equals(Object otherKey) {
        if (this == otherKey) {
            return true;
        }
        if (!(otherKey instanceof TagItemKey other)) {
            return false;
        }

        return Objects.equals(this.getItemId(), other.getItemId()) &&
                Objects.equals(this.getTagId(), other.getTagId());
    }
}
