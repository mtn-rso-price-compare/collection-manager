package mtn.rso.pricecompare.collectionmanager.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "collection")
@NamedQueries(value =
        {
                @NamedQuery(name = "CollectionEntity.getAll",
                        query = "SELECT ce FROM CollectionEntity ce")
        })
public class CollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "name")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
