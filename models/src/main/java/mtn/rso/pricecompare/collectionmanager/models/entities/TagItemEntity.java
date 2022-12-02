package mtn.rso.pricecompare.collectionmanager.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "tag_item")
@IdClass(TagItemKey.class)
@NamedQueries(value =
        {
                @NamedQuery(name = "TagItemEntity.getAll",
                        query = "SELECT tie FROM TagItemEntity tie"),
                @NamedQuery(name = "TagItemEntity.getByTag",
                        query = "SELECT tie FROM TagItemEntity tie WHERE tie.tagId = :tagId")
        })
public class TagItemEntity {
    @Id
    @Column(name = "tag_id")
    private Integer tagId;

    @Id
    @Column(name = "item_id")
    private Integer itemId;

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
}
