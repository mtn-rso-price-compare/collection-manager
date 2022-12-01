package mtn.rso.pricecompare.priceupdater.models.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "store")
@NamedQueries(value =
        {
                @NamedQuery(name = "StoreEntity.getAll",
                        query = "SELECT s FROM StoreEntity s")
        })
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @OneToMany(mappedBy = "storeEntity")
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<PriceEntity> getPriceEntityList() {
        return priceEntityList;
    }

    public void setPriceEntityList(List<PriceEntity> priceEntityList) {
        this.priceEntityList = priceEntityList;
    }

}