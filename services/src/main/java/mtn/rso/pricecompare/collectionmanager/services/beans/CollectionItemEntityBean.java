package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemKey;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;


@Log
@RequestScoped
public class CollectionItemEntityBean {

    private final Logger log = LogManager.getLogger(CollectionItemEntityBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    @Counted(name = "collectionItems_get_all_counter", description = "Displays the total number of getCollectionItemEntity() invocations that have occurred.")
    public List<CollectionItemEntity> getCollectionItemEntity() {

        TypedQuery<CollectionItemEntity> query = em.createNamedQuery("CollectionItemEntity.getAll", CollectionItemEntity.class);
        return query.getResultList();
    }

    // GET request with parameters
    @Counted(name = "collectionItems_get_counter", description = "Displays the total number of getCollectionItemEntity(uriInfo) invocations that have occurred.")
    public List<CollectionItemEntity> getCollectionItemEntityFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();
        return JPAUtils.queryEntities(em, CollectionItemEntity.class, queryParameters);
    }

    // GET by collectionId
    @Counted(name = "collectionItems_get_bycollection_counter", description = "Displays the total number of getCollectionItemEntity(collectionId) invocations that have occurred.")
    public List<CollectionItemEntity> getCollectionItemEntityByCollection(Integer collectionId) {

        TypedQuery<CollectionItemEntity> query = em.createNamedQuery("CollectionItemEntity.getByCollection", CollectionItemEntity.class);
        query.setParameter("collectionId", collectionId);
        return query.getResultList();
    }

    // POST
    // NOTE: This method assumes that collectionItemEntity.getItemId() is a valid item ID
    @Counted(name = "collectionItem_create_counter", description = "Displays the total number of createCollectionItemEntity(collectionId, itemId) invocations that have occurred.")
    public CollectionItemEntity createCollectionItemEntity(Integer collectionId, Integer itemId) {

        CollectionItemEntity collectionItemEntity = new CollectionItemEntity();
        collectionItemEntity.setCollectionId(collectionId);
        collectionItemEntity.setItemId(itemId);

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, collectionItemEntity.getCollectionId());
        if (collectionEntity == null) {
            log.debug("createCollectionItemEntity(collectionId, itemId): did not create entity due to missing relations.");
            throw new NotFoundException();
        }

        try {
            beginTx();
            em.persist(collectionItemEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
            log.warn("createCollectionItemEntity(collectionId, itemId): could not persist entity.");
            throw new RuntimeException("Entity was not persisted");
        }

        return collectionItemEntity;
    }

    // GET by id
    @Counted(name = "collectionItem_get_counter", description = "Displays the total number of getCollectionItemEntity(collectionItemKey) invocations that have occurred.")
    public CollectionItemEntity getCollectionItemEntity(CollectionItemKey collectionItemKey) {

        CollectionItemEntity collectionItemEntity = em.find(CollectionItemEntity.class, collectionItemKey);
        if (collectionItemEntity == null) {
            log.debug("getCollectionItemEntity(collectionItemKey): could not find entity.");
            throw new NotFoundException();
        }

        return collectionItemEntity;
    }

    // DELETE by id
    @Counted(name = "collectionItem_delete_counter", description = "Displays the total number of deleteCollectionItemEntity(collectionItemKey) invocations that have occurred.")
    public boolean deleteCollectionItemEntity(CollectionItemKey collectionItemKey) {

        CollectionItemEntity collectionItemEntity = em.find(CollectionItemEntity.class, collectionItemKey);
        if (collectionItemEntity == null) {
            log.debug("deleteCollectionItemEntity(collectionItemKey): could not find entity.");
            throw new NotFoundException();
        }

        try {
            beginTx();
            em.remove(collectionItemEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.warn("deleteCollectionItemEntity(collectionItemKey): could not remove entity.");
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
