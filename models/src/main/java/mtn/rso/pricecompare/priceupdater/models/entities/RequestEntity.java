package mtn.rso.pricecompare.priceupdater.models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request")
@NamedQueries(value =
        {
                @NamedQuery(name = "RequestEntity.getAll",
                        query = "SELECT r FROM RequestEntity r")
        })
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "status")
    private Integer status;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}