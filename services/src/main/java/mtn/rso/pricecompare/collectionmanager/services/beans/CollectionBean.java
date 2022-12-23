package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.lib.Collection;
import mtn.rso.pricecompare.collectionmanager.models.converters.CollectionConverter;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;


@Log
@RequestScoped
public class CollectionBean {

    private final Logger log = LogManager.getLogger(CollectionBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    @Counted(name = "collections_get_all_counter", description = "Displays the total number of getCollection() invocations that have occurred.")
    public List<Collection> getCollection() {

        TypedQuery<CollectionEntity> query = em.createNamedQuery("CollectionEntity.getAll", CollectionEntity.class);
        List<CollectionEntity> resultList = query.getResultList();
        return resultList.stream().map(ce -> CollectionConverter.toDto(ce, null))
                .collect(Collectors.toList());
    }

    // GET request with parameters
    @Counted(name = "collections_get_counter", description = "Displays the total number of getCollection(uriInfo) invocations that have occurred.")
    public List<Collection> getCollectionFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();
        return JPAUtils.queryEntities(em, CollectionEntity.class, queryParameters).stream()
                .map(ce -> CollectionConverter.toDto(ce, null)).collect(Collectors.toList());
    }

    // POST
    // NOTE: Does not create collection item entities if included. Use CollectionItemEntityBean to persist those.
    @Counted(name = "collection_create_counter", description = "Displays the total number of createCollection(collection) invocations that have occurred.")
    public Collection createCollection(Collection collection) {

        CollectionEntity collectionEntity = CollectionConverter.toEntity(collection);

        try {
            beginTx();
            em.persist(collectionEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (collectionEntity.getId() == null) {
            log.warn("createCollection(collection): could not persist entity.");
            throw new RuntimeException("Entity was not persisted");
        }

        return CollectionConverter.toDto(collectionEntity, null);
    }

    // GET by id
    // NOTE: Does not get collection item entities. Use CollectionItemEntityBean to get them and set manually.
    @Counted(name = "collection_get_counter", description = "Displays the total number of getCollection(id) invocations that have occurred.")
    public Collection getCollection(Integer id) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null) {
            log.debug("getCollection(id): could not find entity.");
            throw new NotFoundException();
        }

        return CollectionConverter.toDto(collectionEntity, null);
    }

    // GET by id
    // If collection item entities were already retrieved, use this method to automatically set them in DTO.
    @Counted(name = "collection_get_withItems_counter", description = "Displays the total number of getCollection(id, collectionItemEntities) invocations that have occurred.")
    public Collection getCollection(Integer id, List<CollectionItemEntity> collectionItemEntities) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null) {
            log.debug("getCollection(id, collectionItemEntities): could not find entity.");
            throw new NotFoundException();
        }

        return CollectionConverter.toDto(collectionEntity, collectionItemEntities);
    }

    // PUT by id
    // NOTE: Does not update collection item entities if included. Use CollectionItemEntityBean to persist those.
    @Counted(name = "collection_put_counter", description = "Displays the total number of putCollection(id, collection) invocations that have occurred.")
    public Collection putCollection(Integer id, Collection collection) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null) {
            log.debug("putCollection(id, collection): could not find entity.");
            throw new NotFoundException();
        }
        CollectionEntity updatedCollectionEntity = CollectionConverter.toEntity(collection);
        CollectionConverter.completeEntity(updatedCollectionEntity, collectionEntity);

        try {
            beginTx();
            updatedCollectionEntity.setId(collectionEntity.getId());
            updatedCollectionEntity = em.merge(updatedCollectionEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.warn("putCollection(id, collection): could not persist entity.");
            throw new RuntimeException("Entity was not persisted");
        }

        return CollectionConverter.toDto(updatedCollectionEntity, null);
    }

    // DELETE by id
    // NOTE: It will fail if collection has associated items. Use CollectionItemEntityBean to delete those first.
    @Counted(name = "collection_delete_counter", description = "Displays the total number of deleteCollection(id) invocations that have occurred.")
    public boolean deleteCollection(Integer id) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null) {
            log.debug("deleteCollection(id): could not find entity.");
            throw new NotFoundException();
        }

        TypedQuery<CollectionItemEntity> query = em.createNamedQuery("CollectionItemEntity.getByCollection", CollectionItemEntity.class);
        query.setParameter("collectionId", id);
        if(!query.getResultList().isEmpty()) {
            log.debug("deleteCollection(id): did not remove entity due to existing relations.");
            return false;
        }

        try {
            beginTx();
            em.remove(collectionEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.warn("deleteCollection(id): could not remove entity.");
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
