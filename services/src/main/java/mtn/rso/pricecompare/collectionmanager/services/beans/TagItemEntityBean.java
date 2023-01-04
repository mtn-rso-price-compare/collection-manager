package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.kumuluz.ee.rest.utils.QueryStringDefaults;
import mtn.rso.pricecompare.collectionmanager.models.entities.*;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;


@Log
@ApplicationScoped
public class TagItemEntityBean {

    private final Logger log = LogManager.getLogger(TagItemEntityBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    @Counted(name = "tagItems_get_all_counter", description = "Displays the total number of getTagItemEntity() invocations that have occurred.")
    public List<TagItemEntity> getTagItemEntity() {

        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getAll", TagItemEntity.class);
        return query.getResultList();
    }

    // GET request with parameters
    @Counted(name = "tagItems_get_counter", description = "Displays the total number of getTagItemEntity(uriInfo) invocations that have occurred.")
    public List<TagItemEntity> getTagItemEntityFilter(UriInfo uriInfo) {

        QueryStringDefaults qsd = new QueryStringDefaults().maxLimit(200).defaultLimit(40).defaultOffset(0);
        QueryParameters query = qsd.builder().queryEncoded(uriInfo.getRequestUri().getRawQuery()).build();
        return JPAUtils.queryEntities(em, TagItemEntity.class, query);
    }

    // GET by tagId
    @Counted(name = "tagItems_get_bytag_counter", description = "Displays the total number of getTagItemEntity(tagId) invocations that have occurred.")
    public List<TagItemEntity> getTagItemEntityByTag(Integer tagId) {

        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getByTag", TagItemEntity.class);
        query.setParameter("tagId", tagId);
        return query.getResultList();
    }

    // POST
    // NOTE: This method assumes that tagItemEntity.getItemId() is a valid item ID
    @Counted(name = "tagItem_create_counter", description = "Displays the total number of createTagItemEntity(tagId, itemId) invocations that have occurred.")
    public TagItemEntity createTagItemEntity(Integer tagId, Integer itemId) {

        TagItemEntity tagItemEntity = new TagItemEntity();
        tagItemEntity.setTagId(tagId);
        tagItemEntity.setItemId(itemId);

        TagEntity tagEntity = em.find(TagEntity.class, tagItemEntity.getItemId());
        if (tagEntity == null) {
            log.debug("createTagItemEntity(tagId, itemId): did not create entity due to missing relations.");
            throw new NotFoundException();
        }

        try {
            beginTx();
            em.persist(tagItemEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
            log.error("createTagItemEntity(tagId, itemId): could not persist entity.", e);
            throw new RuntimeException("Entity was not persisted");
        }

        return tagItemEntity;
    }

    // GET by id
    @Counted(name = "tagItem_get_counter", description = "Displays the total number of getTagItemEntity(tagItemKey) invocations that have occurred.")
    public TagItemEntity getTagItemEntity(TagItemKey tagItemKey) {

        TagItemEntity tagItemEntity = em.find(TagItemEntity.class, tagItemKey);
        if (tagItemEntity == null) {
            log.debug("getTagItemEntity(tagItemKey): could not find entity.");
            throw new NotFoundException();
        }

        return tagItemEntity;
    }

    // DELETE by id
    @Counted(name = "tagItem_delete_counter", description = "Displays the total number of deleteTagItemEntity(tagItemKey) invocations that have occurred.")
    public boolean deleteTagItemEntity(TagItemKey tagItemKey) {

        TagItemEntity tagItemEntity = em.find(TagItemEntity.class, tagItemKey);
        if (tagItemEntity == null) {
            log.debug("deleteTagItemEntity(tagItemKey): could not find entity.");
            throw new NotFoundException();
        }

        try {
            beginTx();
            em.remove(tagItemEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.error("deleteTagItemEntity(tagItemKey): could not remove entity.", e);
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
