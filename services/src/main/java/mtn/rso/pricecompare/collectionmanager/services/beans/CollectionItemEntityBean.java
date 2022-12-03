package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemKey;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;


@RequestScoped
public class CollectionItemEntityBean {

    private Logger log = Logger.getLogger(CollectionItemEntityBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    public List<CollectionItemEntity> getCollectionItemEntity() {

        TypedQuery<CollectionItemEntity> query = em.createNamedQuery("CollectionItemEntity.getAll", CollectionItemEntity.class);
        return query.getResultList();
    }

    // GET request with parameters
    public List<CollectionItemEntity> getCollectionItemEntityFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();

        return JPAUtils.queryEntities(em, CollectionItemEntity.class, queryParameters);
    }

    // GET by collectionId
    public List<CollectionItemEntity> getCollectionItemEntityByCollection(Integer collectionId) {
        TypedQuery<CollectionItemEntity> query = em.createNamedQuery("CollectionItemEntity.getByCollection", CollectionItemEntity.class);

        query.setParameter("collectionId", collectionId);
        return query.getResultList();
    }

    // POST
    // NOTE: This method assumes that collectionItemEntity.getItemId() is a valid item ID
    public CollectionItemEntity createCollectionItemEntity(Integer collectionId, Integer itemId) {
        CollectionItemEntity collectionItemEntity = new CollectionItemEntity();
        collectionItemEntity.setCollectionId(collectionId);
        collectionItemEntity.setItemId(itemId);

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, collectionItemEntity.getCollectionId());
        if (collectionEntity == null)
            throw new NotFoundException();

        try {
            beginTx();
            em.persist(collectionItemEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
            throw new RuntimeException("Entity was not persisted");
        }

        return collectionItemEntity;
    }

    // GET by id
    public CollectionItemEntity getCollectionItemEntity(CollectionItemKey collectionItemKey) {
        CollectionItemEntity collectionItemEntity = em.find(CollectionItemEntity.class, collectionItemKey);
        if (collectionItemEntity == null)
            throw new NotFoundException();

        return collectionItemEntity;
    }

    // DELETE by id
    public boolean deleteCollectionItemEntity(CollectionItemKey collectionItemKey) {
        CollectionItemEntity collectionItemEntity = em.find(CollectionItemEntity.class, collectionItemKey);
        if (collectionItemEntity == null)
            throw new NotFoundException();

        try {
            beginTx();
            em.remove(collectionItemEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
