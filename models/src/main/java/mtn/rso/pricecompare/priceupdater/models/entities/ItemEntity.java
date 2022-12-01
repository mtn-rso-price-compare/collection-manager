package mtn.rso.pricecompare.priceupdater.models.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "item")
@NamedQueries(value =
        {
                @NamedQuery(name = "ItemEntity.getAll",
                        query = "SELECT i FROM ItemEntity i")
        })
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "itemEntity")
    List<PriceEntity> priceEntityList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PriceEntity> getPriceEntityList() {
        return priceEntityList;
    }

    public void setPriceEntityList(List<PriceEntity> priceEntityList) {
        this.priceEntityList = priceEntityList;
    }

}