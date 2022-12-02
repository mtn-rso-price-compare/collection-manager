package mtn.rso.pricecompare.collectionmanager.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class CollectionItemKey implements Serializable {

    private Integer collectionId;

    private Integer itemId;

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Override
    public int hashCode() {
        return (Objects.hash(this.getItemId(), this.getCollectionId()));
    }

    @Override
    public boolean equals(Object otherKey) {
        if (this == otherKey) {
            return true;
        }
        if (!(otherKey instanceof CollectionItemKey other)) {
            return false;
        }

        return Objects.equals(this.getItemId(), other.getItemId()) &&
                Objects.equals(this.getCollectionId(), other.getCollectionId());
    }
}
