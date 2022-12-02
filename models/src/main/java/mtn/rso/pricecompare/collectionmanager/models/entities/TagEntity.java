package mtn.rso.pricecompare.collectionmanager.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "tag")
@NamedQueries(value =
        {
                @NamedQuery(name = "TagEntity.getAll",
                        query = "SELECT te FROM TagEntity te")
        })
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

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

}
