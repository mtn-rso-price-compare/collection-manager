package mtn.rso.pricecompare.priceupdater.models.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;

@Embeddable
public class PriceKey implements Serializable {
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "store_id")
    private Integer storeId;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    @Override
    public int hashCode() {
        return (Objects.hash(this.getItemId(), this.getStoreId()));
    }

    @Override
    public boolean equals(Object otherKey) {
        if (this == otherKey) {
            return true;
        }
        if (!(otherKey instanceof PriceKey other)) {
            return false;
        }

        return Objects.equals(this.getItemId(), other.getItemId()) &&
                Objects.equals(this.getStoreId(), other.getStoreId());
    }
}
