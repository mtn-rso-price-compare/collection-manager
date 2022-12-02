package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.lib.Collection;
import mtn.rso.pricecompare.collectionmanager.models.converters.CollectionConverter;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class CollectionBean {

    private Logger log = Logger.getLogger(CollectionBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    public List<Collection> getCollection() {

        TypedQuery<CollectionEntity> query = em.createNamedQuery("CollectionEntity.getAll", CollectionEntity.class);
        List<CollectionEntity> resultList = query.getResultList();

        return resultList.stream().map(ce -> CollectionConverter.toDto(ce, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    // GET request with parameters
    public List<Collection> getCollectionFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();

        return JPAUtils.queryEntities(em, CollectionEntity.class, queryParameters).stream()
                .map(ce -> CollectionConverter.toDto(ce, Collections.emptyList())).collect(Collectors.toList());
    }

    // POST
    // NOTE: Does not create collection item entities if included. Use CollectionItemEntityBean to persist those.
    public Collection createCollection(Collection collection) {

        CollectionEntity collectionEntity = CollectionConverter.toEntity(collection);

        try {
            beginTx();
            em.persist(collectionEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (collectionEntity.getId() == null)
            throw new RuntimeException("Entity was not persisted");
        return CollectionConverter.toDto(collectionEntity, Collections.emptyList());
    }

    // GET by id
    // NOTE: Does not get collection item entities. Use CollectionItemEntityBean to get them and set manually.
    public Collection getCollection(Integer id) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null)
            throw new NotFoundException();

        return CollectionConverter.toDto(collectionEntity, Collections.emptyList());
    }

    // PUT by id
    // NOTE: Does not update collection item entities if included. Use CollectionItemEntityBean to persist those.
    public Collection putCollection(Integer id, Collection collection) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null)
            throw new NotFoundException();
        CollectionEntity updatedCollectionEntity = CollectionConverter.toEntity(collection);

        try {
            beginTx();
            updatedCollectionEntity.setId(collectionEntity.getId());
            updatedCollectionEntity = em.merge(updatedCollectionEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            throw new RuntimeException("Entity was not persisted");
        }

        return CollectionConverter.toDto(updatedCollectionEntity, Collections.emptyList());
    }

    // DELETE by id
    // NOTE: It will fail if collection has associated items. Use CollectionItemEntityBean to delete those first.
    public boolean deleteCollection(Integer id) {

        CollectionEntity collectionEntity = em.find(CollectionEntity.class, id);
        if (collectionEntity == null)
            throw new NotFoundException();

        // TODO: Check if collection has items associated

        try {
            beginTx();
            em.remove(collectionEntity);
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
