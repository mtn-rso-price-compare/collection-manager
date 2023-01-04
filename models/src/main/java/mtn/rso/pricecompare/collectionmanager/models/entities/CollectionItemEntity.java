package mtn.rso.pricecompare.collectionmanager.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "collection_item")
@IdClass(CollectionItemKey.class)
@NamedQueries(value =
        {
                @NamedQuery(name = "CollectionItemEntity.getAll",
                        query = "SELECT cie FROM CollectionItemEntity cie"),
                @NamedQuery(name = "CollectionItemEntity.getByCollection",
                        query = "SELECT cie FROM CollectionItemEntity cie WHERE cie.collectionId = :collectionId")
        })
public class CollectionItemEntity {
    @Id
    @Column(name = "collection_id")
    private Integer collectionId;

    @Id
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "amount")
    private Integer amount;

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
