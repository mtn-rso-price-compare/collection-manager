package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.models.entities.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;


@RequestScoped
public class TagItemEntityBean {

    private Logger log = Logger.getLogger(TagItemEntityBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    public List<TagItemEntity> getTagItemEntity() {

        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getAll", TagItemEntity.class);
        return query.getResultList();
    }

    // GET request with parameters
    public List<TagItemEntity> getTagItemEntityFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();

        return JPAUtils.queryEntities(em, TagItemEntity.class, queryParameters);
    }

    // GET by tagId
    public List<TagItemEntity> getTagItemEntityByTag(Integer tagId) {
        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getByTag", TagItemEntity.class);

        query.setParameter("tagId", tagId);
        return query.getResultList();
    }

    // POST
    // NOTE: This method assumes that tagItemEntity.getItemId() is a valid item ID
    public TagItemEntity createTagItemEntity(Integer tagId, Integer itemId) {
        TagItemEntity tagItemEntity = new TagItemEntity();
        tagItemEntity.setTagId(tagId);
        tagItemEntity.setItemId(itemId);

        TagEntity tagEntity = em.find(TagEntity.class, tagItemEntity.getItemId());
        if (tagEntity == null)
            throw new NotFoundException();

        try {
            beginTx();
            em.persist(tagItemEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
            throw new RuntimeException("Entity was not persisted");
        }

        return tagItemEntity;
    }

    // GET by id
    public TagItemEntity getTagItemEntity(TagItemKey tagItemKey) {
        TagItemEntity tagItemEntity = em.find(TagItemEntity.class, tagItemKey);
        if (tagItemEntity == null)
            throw new NotFoundException();

        return tagItemEntity;
    }

    // DELETE by id
    public boolean deleteTagItemEntity(TagItemKey tagItemKey) {
        TagItemEntity tagItemEntity = em.find(TagItemEntity.class, tagItemKey);
        if (tagItemEntity == null)
            throw new NotFoundException();

        try {
            beginTx();
            em.remove(tagItemEntity);
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