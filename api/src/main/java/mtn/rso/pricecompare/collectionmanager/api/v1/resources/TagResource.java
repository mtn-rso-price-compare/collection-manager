package mtn.rso.pricecompare.collectionmanager.api.v1.resources;

import mtn.rso.pricecompare.collectionmanager.lib.Item;
import mtn.rso.pricecompare.collectionmanager.lib.Tag;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.TagItemKey;
import mtn.rso.pricecompare.collectionmanager.services.beans.TagBean;
import mtn.rso.pricecompare.collectionmanager.services.beans.TagItemEntityBean;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;


@ApplicationScoped
@Path("/tag")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

    private Logger log = Logger.getLogger(TagResource.class.getName());

    @Inject
    private TagBean tagBean;

    @Inject
    private TagItemEntityBean tagItemEntityBean;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get a list of all tags in the database.", summary = "Get all tags")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of tags",
                    headers = {@Header(
                            name = "X-Total-Count",
                            description = "Number of objects in list"
                    )},
                    content = @Content(schema = @Schema(implementation = Tag.class, type = SchemaType.ARRAY))
            )
    })
    @GET
    public Response getTag() {
        List<Tag> tagList = tagBean.getTagFilter(uriInfo);
        return Response.status(Response.Status.OK).header("X-Total-Count", tagList.size()).entity(tagList).build();
    }

    @Operation(description = "Create a new tag for items.", summary = "Create tag")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Tag successfully created",
                    content = @Content(schema = @Schema(implementation = Tag.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in persisting tag"
            )
    })
    @POST
    public Response createTag(@RequestBody(description = "Tag DTO (without items)", required = true,
            content = @Content(schema = @Schema(implementation = Tag.class))) Tag tag) {

        if(tag.getTagName() == null || tag.getTagName().equals(""))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            tag = tagBean.createTag(tag);
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.CREATED).entity(tag).build();
    }


    @Operation(description = "Get a tag and its items by tag ID.", summary = "Get tag items")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Tag",
                    content = @Content(schema = @Schema(implementation = Tag.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tag not found"
            )
    })
    @GET
    @Path("/{tagId}")
    public Response getTag(@Parameter(name = "tag ID", required = true)
                               @PathParam("tagId") Integer tagId) {

        Tag tag;
        try {
            List<TagItemEntity> tagItemEntities = tagItemEntityBean
                    .getTagItemEntityByTag(tagId);
            tag = tagBean.getTag(tagId, tagItemEntities);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(tag).build();
    }

    @Operation(description = "Update information about a tag.", summary = "Update tag")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Tag successfully updated",
                    content = @Content(schema = @Schema(implementation = Tag.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tag not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in persisting tag"
            )
    })
    @PUT
    @Path("{tagId}")
    public Response putTag(@RequestBody(description = "Tag DTO (without items)", required = true,
            content = @Content(schema = @Schema(implementation = Tag.class))) Tag tag,
                           @Parameter(name = "tag ID", required = true)
                           @PathParam("tagId") Integer tagId) {

        if(tag.getTagName() != null && tag.getTagName().equals(""))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            tag = tagBean.putTag(tagId, tag);
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch(RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.OK).entity(tag).build();
    }

    @Operation(description = "Delete a tag and its items.", summary = "Delete tag")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Tag successfully deleted"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tag not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in deleting tag"
            )
    })
    @DELETE
    @Path("{tagId}")
    public Response deleteTag(@Parameter(name = "tag ID", required = true)
                                  @PathParam("tagId") Integer tagId) {

        List<TagItemEntity> tagItemEntities = tagItemEntityBean.getTagItemEntityByTag(tagId);

        boolean success = true;
        for (TagItemEntity tie : tagItemEntities) {
            try {
                success &= tagItemEntityBean.deleteTagItemEntity(
                        new TagItemKey(tagId, tie.getItemId()));
            } catch(NotFoundException e) {
                success = false;
            }
        }

        if(!success)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        try {
            success = tagBean.deleteTag(tagId);
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(success)
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(description = "Add a tag to an item.", summary = "Tag an item")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Item successfully tagged"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tag not found"
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Item already tagged"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in tagging item"
            )
    })
    @POST
    @Path("{tagId}")
    public Response createTagItem(@RequestBody(description = "Item DTO (itemId only)", required = true,
            content = @Content(schema = @Schema(implementation = Item.class))) Item item,
                                  @Parameter(name = "tag ID", required = true)
                                  @PathParam("tagId") Integer tagId
    ) {
        if(item.getItemId() == null)
            return Response.status(Response.Status.BAD_REQUEST).build();
        // TODO: Lookup itemId in price ms

        boolean resourceExists = true;
        try {
            tagItemEntityBean.getTagItemEntity(new TagItemKey(tagId, item.getItemId()));
        } catch (NotFoundException e) {
            resourceExists = false;
        }
        if(resourceExists)
            return Response.status(Response.Status.CONFLICT).build();

        try {
            tagItemEntityBean.createTagItemEntity(tagId, item.getItemId());
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Operation(description = "Remove a tag from an item.", summary = "Untag an item")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Item successfully untagged"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tag not found for item"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in untagging item"
            )
    })
    @DELETE
    @Path("{tagId}/{itemId}")
    public Response deleteTagItem(@Parameter(name = "tag ID", required = true)
                                      @PathParam("tagId") Integer tagId,
                                  @Parameter(name = "item ID", required = true)
                                  @PathParam("itemId") Integer itemId) {
        boolean success;
        try {
            success = tagItemEntityBean.deleteTagItemEntity(new TagItemKey(tagId, itemId));
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(success)
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
