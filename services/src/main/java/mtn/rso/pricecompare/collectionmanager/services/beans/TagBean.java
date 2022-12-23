package mtn.rso.pricecompare.collectionmanager.services.beans;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import mtn.rso.pricecompare.collectionmanager.lib.Tag;
import mtn.rso.pricecompare.collectionmanager.models.converters.TagConverter;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;
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
public class TagBean {

    private final Logger log = LogManager.getLogger(TagBean.class.getName());

    @Inject
    private EntityManager em;

    // generic GET query for all entities
    @Counted(name = "tags_get_all_counter", description = "Displays the total number of getTag() invocations that have occurred.")
    public List<Tag> getTag() {

        TypedQuery<TagEntity> query = em.createNamedQuery("TagEntity.getAll", TagEntity.class);
        List<TagEntity> resultList = query.getResultList();
        return resultList.stream().map(te -> TagConverter.toDto(te, null)).collect(Collectors.toList());
    }

    // GET request with parameters
    @Counted(name = "tags_get_counter", description = "Displays the total number of getTag(uriInfo) invocations that have occurred.")
    public List<Tag> getTagFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).build();
        return JPAUtils.queryEntities(em, TagEntity.class, queryParameters).stream()
                .map(te -> TagConverter.toDto(te, null)).collect(Collectors.toList());
    }

    // POST
    // NOTE: Does not create tag item entities if included. Use TagItemEntityBean to persist those.
    @Counted(name = "tag_create_counter", description = "Displays the total number of createTag(tag) invocations that have occurred.")
    public Tag createTag(Tag tag) {

        TagEntity tagEntity = TagConverter.toEntity(tag);

        try {
            beginTx();
            em.persist(tagEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (tagEntity.getId() == null) {
            log.warn("createTag(tag): could not persist entity.");
            throw new RuntimeException("Entity was not persisted");
        }

        return TagConverter.toDto(tagEntity, null);
    }

    // GET by id
    // NOTE: Does not get tag item entities. Use TagItemEntityBean to get them and set manually.
    @Counted(name = "tag_get_counter", description = "Displays the total number of getTag(id) invocations that have occurred.")
    public Tag getTag(Integer id) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null) {
            log.debug("getTag(id): could not find entity.");
            throw new NotFoundException();
        }

        return TagConverter.toDto(tagEntity, null);
    }

    // GET by id
    // If collection item entities were already retrieved, use this method to automatically set them in DTO.
    @Counted(name = "tag_get_withItems_counter", description = "Displays the total number of getTag(id, tagItemEntities) invocations that have occurred.")
    public Tag getTag(Integer id, List<TagItemEntity> tagItemEntities) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null) {
            log.debug("getTag(id, tagItemEntities): could not find entity.");
            throw new NotFoundException();
        }

        return TagConverter.toDto(tagEntity, tagItemEntities);
    }

    // PUT by id
    // NOTE: Does not update tag item entities if included. Use TagItemEntityBean to persist those.
    @Counted(name = "tag_put_counter", description = "Displays the total number of putTag(id, tag) invocations that have occurred.")
    public Tag putTag(Integer id, Tag tag) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null) {
            log.debug("putTag(id, tag): could not find entity.");
            throw new NotFoundException();
        }
        TagEntity updatedTagEntity = TagConverter.toEntity(tag);
        TagConverter.completeTag(updatedTagEntity, tagEntity);

        try {
            beginTx();
            updatedTagEntity.setId(tagEntity.getId());
            updatedTagEntity = em.merge(updatedTagEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.warn("putTag(id, tag): could not persist entity.");
            throw new RuntimeException("Entity was not persisted");
        }

        return TagConverter.toDto(updatedTagEntity, null);
    }

    // DELETE by id
    // NOTE: It will fail if tag has associated items. Use TagItemEntityBean to delete those first.
    @Counted(name = "tag_delete_counter", description = "Displays the total number of deleteTag(id) invocations that have occurred.")
    public boolean deleteTag(Integer id) {

        TagEntity tagEntity = em.find(TagEntity.class, id);
        if (tagEntity == null) {
            log.debug("deleteTag(id): could not find entity.");
            throw new NotFoundException();
        }

        TypedQuery<TagItemEntity> query = em.createNamedQuery("TagItemEntity.getByTag", TagItemEntity.class);
        query.setParameter("tagId", id);
        if(!query.getResultList().isEmpty()) {
            log.debug("deleteTag(id): did not remove entity due to existing relations.");
            return false;
        }

        try {
            beginTx();
            em.remove(tagEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
            log.warn("deleteTag(id): could not remove entity.");
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
