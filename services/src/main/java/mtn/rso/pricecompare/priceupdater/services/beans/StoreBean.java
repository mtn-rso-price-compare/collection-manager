package mtn.rso.pricecompare.priceupdater.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.priceupdater.lib.Store;
import mtn.rso.pricecompare.priceupdater.models.converters.StoreConverter;
import mtn.rso.pricecompare.priceupdater.models.entities.StoreEntity;

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
public class StoreBean {

    private Logger log = Logger.getLogger(StoreBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    public List<Store> getStore() {

        TypedQuery<StoreEntity> query = em.createNamedQuery("StoreEntity.getAll", StoreEntity.class);
        List<StoreEntity> resultList = query.getResultList();

        return resultList.stream().map(se -> StoreConverter.toDto(se, false)).collect(Collectors.toList());
    }

    // GET request with parameters
    public List<Store> getStoreFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();

        return JPAUtils.queryEntities(em, StoreEntity.class, queryParameters).stream()
                .map(se -> StoreConverter.toDto(se, false)).collect(Collectors.toList());
    }

    // POST
    // NOTE: Does not create price entities if included. Use PriceBean to persist those.
    public Store createStore(Store store) {
        StoreEntity storeEntity = StoreConverter.toEntity(store, Collections.emptyList());

        try {
            beginTx();
            em.persist(storeEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (storeEntity.getId() == null)
            throw new RuntimeException("Entity was not persisted");
        return StoreConverter.toDto(storeEntity, false);
    }

    // GET by id
    public Store getStore(Integer id) {

        StoreEntity storeEntity = em.find(StoreEntity.class, id);
        if (storeEntity == null)
            throw new NotFoundException();

        return StoreConverter.toDto(storeEntity, true);
    }

    // PUT by id
    // NOTE: Does not update price entities if included. Use PriceBean to persist those.
    public Store putStore(Integer id, Store store) {

        StoreEntity storeEntity = em.find(StoreEntity.class, id);
        if (storeEntity == null)
            throw new NotFoundException();
        StoreEntity updatedStoreEntity = StoreConverter.toEntity(store, storeEntity.getPriceEntityList());

        try {
            beginTx();
            updatedStoreEntity.setId(storeEntity.getId());
            updatedStoreEntity = em.merge(updatedStoreEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return StoreConverter.toDto(updatedStoreEntity, false);
    }

    // DELETE by id
    // NOTE: It will fail if store has associated prices. Use PriceBean to delete those first.
    public boolean deleteStore(Integer id) {

        StoreEntity storeEntity = em.find(StoreEntity.class, id);
        if (storeEntity == null)
            throw new NotFoundException();
        else if(!storeEntity.getPriceEntityList().isEmpty())
            return false;

        try {
            beginTx();
            em.remove(storeEntity);
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
