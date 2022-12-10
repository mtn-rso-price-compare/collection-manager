package mtn.rso.pricecompare.collectionmanager.api.v1.resources;

import mtn.rso.pricecompare.collectionmanager.lib.Collection;
import mtn.rso.pricecompare.collectionmanager.lib.Item;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemKey;
import mtn.rso.pricecompare.collectionmanager.services.beans.CollectionBean;
import mtn.rso.pricecompare.collectionmanager.services.beans.CollectionItemEntityBean;
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
@Path("/collection")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollectionResource {

    private Logger log = Logger.getLogger(CollectionResource.class.getName());

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionItemEntityBean collectionItemEntityBean;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get a list of all collections in the database.", summary = "Get all collections")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of collections",
                    headers = {@Header(
                            name = "X-Total-Count",
                            description = "Number of objects in list"
                    )},
                    content = @Content(schema = @Schema(implementation = Collection.class, type = SchemaType.ARRAY))
            )
    })
    @GET
    public Response getCollection() {
        List<Collection> collectionList = collectionBean.getCollectionFilter(uriInfo);
        return Response.status(Response.Status.OK).header("X-Total-Count", collectionList.size()).entity(collectionList).build();
    }

    @Operation(description = "Create a new collection of items.", summary = "Create collection")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Collection successfully created",
                    content = @Content(schema = @Schema(implementation = Collection.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in persisting collection"
            )
    })
    @POST
    public Response createCollection(@RequestBody(description = "Collection DTO (without items)", required = true,
            content = @Content(schema = @Schema(implementation = Collection.class))) Collection collection) {

        if(collection.getUserId() == null || collection.getCollectionName() == null ||
                collection.getCollectionName().equals(""))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            collection = collectionBean.createCollection(collection);
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.CREATED).entity(collection).build();
    }

    @Operation(description = "Get a collection and its items by collection ID.", summary = "Get collection items")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Collection",
                    content = @Content(schema = @Schema(implementation = Collection.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Collection not found"
            )
    })
    @GET
    @Path("/{collectionId}")
    public Response getCollection(@Parameter(name = "collection ID", required = true)
                                      @PathParam("collectionId") Integer collectionId) {

        Collection collection;
        try {
            List<CollectionItemEntity> collectionItemEntities = collectionItemEntityBean
                    .getCollectionItemEntityByCollection(collectionId);
            collection = collectionBean.getCollection(collectionId, collectionItemEntities);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(collection).build();
    }

    @Operation(description = "Update information about a collection.", summary = "Update collection")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Collection successfully updated",
                    content = @Content(schema = @Schema(implementation = Collection.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Collection not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in persisting collection"
            )
    })
    @PUT
    @Path("{collectionId}")
    public Response putCollection(@RequestBody(description = "Collection DTO (without items)", required = true,
            content = @Content(schema = @Schema(implementation = Collection.class))) Collection collection,
                                  @Parameter(name = "collection ID", required = true)
                                  @PathParam("collectionId") Integer collectionId) {

        if(collection.getCollectionName() != null && collection.getCollectionName().equals(""))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            collection = collectionBean.putCollection(collectionId, collection);
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch(RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.OK).entity(collection).build();
    }

    @Operation(description = "Delete a collection and its items.", summary = "Delete collection")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Collection successfully deleted"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Collection not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in deleting collection"
            )
    })
    @DELETE
    @Path("{collectionId}")
    public Response deleteCollection(@Parameter(name = "collection ID", required = true)
                                         @PathParam("collectionId") Integer collectionId) {

        List<CollectionItemEntity> collectionItemEntities = collectionItemEntityBean
                .getCollectionItemEntityByCollection(collectionId);

        boolean success = true;
        for (CollectionItemEntity cie : collectionItemEntities) {
            try {
                success &= collectionItemEntityBean.deleteCollectionItemEntity(
                        new CollectionItemKey(collectionId, cie.getItemId()));
            } catch(NotFoundException e) {
                success = false;
            }
        }

        if(!success)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        try {
            success = collectionBean.deleteCollection(collectionId);
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(success)
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(description = "Add an item to a collection.", summary = "Add collection item")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Item successfully added"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Collection not found"
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Item already in collection"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in adding item"
            )
    })
    @POST
    @Path("{collectionId}")
    public Response createCollectionItem(@RequestBody(description = "Item DTO (itemId only)", required = true,
            content = @Content(schema = @Schema(implementation = Item.class))) Item item,
                                         @Parameter(name = "collection ID", required = true)
                                         @PathParam("collectionId") Integer collectionId) {
        if(item.getItemId() == null)
            return Response.status(Response.Status.BAD_REQUEST).build();
        // TODO: Lookup itemId in price ms

        boolean resourceExists = true;
        try {
            collectionItemEntityBean.getCollectionItemEntity(new CollectionItemKey(collectionId, item.getItemId()));
        } catch (NotFoundException e) {
            resourceExists = false;
        }
        if(resourceExists)
            return Response.status(Response.Status.CONFLICT).build();

        try {
            collectionItemEntityBean.createCollectionItemEntity(collectionId, item.getItemId());
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Operation(description = "Remove an item from a collection.", summary = "Remove collection item")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Item successfully removed"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Item not found in collection"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in removing item"
            )
    })
    @DELETE
    @Path("{collectionId}/{itemId}")
    public Response deleteCollectionItem(@Parameter(name = "collection ID", required = true)
                                             @PathParam("collectionId") Integer collectionId,
                                         @Parameter(name = "item ID", required = true)
                                         @PathParam("itemId") Integer itemId) {
        boolean success;
        try {
            success = collectionItemEntityBean.deleteCollectionItemEntity(new CollectionItemKey(collectionId, itemId));
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(success)
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
