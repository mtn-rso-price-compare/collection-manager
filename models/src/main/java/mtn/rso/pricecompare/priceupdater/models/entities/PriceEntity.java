package mtn.rso.pricecompare.priceupdater.models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "price")
@NamedQueries(value =
        {
                @NamedQuery(name = "PriceEntity.getAll",
                        query = "SELECT p FROM PriceEntity p")
        })
public class PriceEntity {

    @EmbeddedId
    private PriceKey id;

    @ManyToOne()
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private ItemEntity itemEntity;

    @ManyToOne()
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private StoreEntity storeEntity;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public PriceKey getId() {
        return id;
    }

    public void setId(PriceKey id) {
        this.id = id;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }

    public StoreEntity getStoreEntity() {
        return storeEntity;
    }

    public void setStoreEntity(StoreEntity storeEntity) {
        this.storeEntity = storeEntity;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}