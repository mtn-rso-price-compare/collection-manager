package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.lib.Tag;
import mtn.rso.pricecompare.collectionmanager.models.converters.TagConverter;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;

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
public class TagBean {

    private Logger log = Logger.getLogger(TagBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    public List<Tag> getTag() {

        TypedQuery<TagEntity> query = em.createNamedQuery("TagEntity.getAll", TagEntity.class);
        List<TagEntity> resultList = query.getResultList();

        return resultList.stream().map(te -> TagConverter.toDto(te, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    // GET request with parameters
    public List<Tag> getTagFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();

        return JPAUtils.queryEntities(em, TagEntity.class, queryParameters).stream()
                .map(te -> TagConverter.toDto(te, Collections.emptyList())).collect(Collectors.toList());
    }

    // POST
    // NOTE: Does not create tag item entities if included. Use TagItemEntityBean to persist those.
    public Tag createTag(Tag tag) {

        TagEntity tagEntity = TagConverter.toEntity(tag);

        try {
            beginTx();
            em.persist(tagEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (tagEntity.getId() == null)
            throw new RuntimeException("Entity was not persisted");
        return TagConverter.toDto(tagEntity, Collections.emptyList());
    }

    // GET by id
    // NOTE: Does not get tag item entities. Use TagItemEntityBean to get them and set manually.
    public Tag getTag(Integer id) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null)
            throw new NotFoundException();

        return TagConverter.toDto(tagEntity, Collections.emptyList());
    }

    // GET by id
    // If collection item entities were already retrieved, use this method to automatically set them in DTO.
    public Tag getTag(Integer id, List<TagItemEntity> tagItemEntities) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null)
            throw new NotFoundException();

        return TagConverter.toDto(tagEntity, tagItemEntities);
    }

    // PUT by id
    // NOTE: Does not update tag item entities if included. Use TagItemEntityBean to persist those.
    public Tag putTag(Integer id, Tag tag) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null)
            throw new NotFoundException();
        TagEntity updatedTagEntity = TagConverter.toEntity(tag);

        try {
            beginTx();
            updatedTagEntity.setId(tagEntity.getId());
            updatedTagEntity = em.merge(updatedTagEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            throw new RuntimeException("Entity was not persisted");
        }

        return TagConverter.toDto(updatedTagEntity, Collections.emptyList());
    }

    // DELETE by id
    // NOTE: It will fail if tag has associated items. Use TagItemEntityBean to delete those first.
    public boolean deleteTag(Integer id) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null)
            throw new NotFoundException();

        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getByTag", TagItemEntity.class);
        query.setParameter("tagId", id);
        if(!query.getResultList().isEmpty())
            return false;

        try {
            beginTx();
            em.remove(tagEntity);
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
