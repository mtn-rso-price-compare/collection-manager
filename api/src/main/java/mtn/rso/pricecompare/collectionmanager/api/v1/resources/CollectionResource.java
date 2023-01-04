package mtn.rso.pricecompare.collectionmanager.api.v1.resources;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import mtn.rso.pricecompare.collectionmanager.lib.Collection;
import mtn.rso.pricecompare.collectionmanager.lib.ItemDTO;
import mtn.rso.pricecompare.collectionmanager.lib.Price;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemEntity;
import mtn.rso.pricecompare.collectionmanager.models.entities.CollectionItemKey;
import mtn.rso.pricecompare.collectionmanager.services.beans.CollectionBean;
import mtn.rso.pricecompare.collectionmanager.services.beans.CollectionItemEntityBean;
import mtn.rso.pricecompare.collectionmanager.services.clients.PriceUpdaterClient;
import mtn.rso.pricecompare.collectionmanager.services.config.ApiProperties;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;


@Log
@ApplicationScoped
@Path("/collection")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollectionResource {

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionItemEntityBean collectionItemEntityBean;

    @Inject
    private PriceUpdaterClient priceUpdaterClient;

    @Inject
    private ApiProperties apiProperties;

    @Context
    protected UriInfo uriInfo;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final Logger log = LogManager.getLogger(CollectionResource.class.getName());

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
        return Response.status(Response.Status.OK).header("X-Total-Count", collectionList.size())
                .entity(collectionList).build();
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

        if(collection == null || collection.getUserId() == null || collection.getCollectionName() == null ||
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
    public CompletionStage<Response> getCollection(@Parameter(name = "collection ID", required = true)
                                                       @PathParam("collectionId") Integer collectionId,
                                                   @Parameter(name = "returnPrice",
                                        description = "Determines if information for items should be returned.")
                                                   @QueryParam("returnItemInfo") boolean returnItemInfo) {

        // Create new async response, check that itemId is provided and create async request to price updater
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();

        if(!uriInfo.getQueryParameters().containsKey("returnItemInfo"))
            returnItemInfo = apiProperties.getReturnCollectionItemInformation();

        Collection collection;
        try {
            List<CollectionItemEntity> collectionItemEntities = collectionItemEntityBean
                    .getCollectionItemEntityByCollection(collectionId);
            collection = collectionBean.getCollection(collectionId, collectionItemEntities);
        } catch (NotFoundException e) {
            asyncResponse.complete(Response.status(Response.Status.NOT_FOUND).build());
            return asyncResponse;
        }

        if(!returnItemInfo) {
            asyncResponse.complete(Response.status(Response.Status.OK).entity(collection).build());
            return asyncResponse;
        }

        HashMap<Integer, Future<Response>> updaterRequests = new HashMap<>();
        for(ItemDTO item : collection.getItemList())
            updaterRequests.put(item.getItemId(),
                    priceUpdaterClient.getItemRequest(item.getItemId(), true).async().get());

        collection.setPriceTotal(new ArrayList<>());
        HashMap<Integer, Price> totalPrices = new HashMap<>();

        executor.execute( () -> {
            for(ItemDTO item : collection.getItemList()) {
                if(updaterRequests.containsKey(item.getItemId())) {
                    try {
                        // Try to retrieve response from price updater and check its status
                        Response updaterResponse = updaterRequests.get(item.getItemId()).get();
                        switch (updaterResponse.getStatus()) {
                            case 200 -> {
                                ItemDTO receivedItem = updaterResponse.readEntity(ItemDTO.class);
                                item.setItemName(receivedItem.getItemName());
                                item.setPriceList(receivedItem.getPriceList());
                                if(item.getAmount() != null && item.getAmount() > 0) {
                                    for (Price price : receivedItem.getPriceList()) {
                                        if (!totalPrices.containsKey(price.getStoreId())) {
                                            Price newTotalPrice = new Price();
                                            newTotalPrice.setStoreId(price.getStoreId());
                                            newTotalPrice.setAmount(price.getAmount() * item.getAmount());
                                            collection.getPriceTotal().add(newTotalPrice);
                                            totalPrices.put(price.getStoreId(), newTotalPrice);
                                        } else {
                                            Price totalPrice = totalPrices.get(price.getStoreId());
                                            totalPrice.setAmount(totalPrice.getAmount() +
                                                    price.getAmount() * item.getAmount());
                                        }
                                    }
                                }
                            }
                            case 404 -> log.error(String.format("getCollection(collectionId, returnPrice): " +
                                            "price-updater could not find item (itemId=%d). " +
                                            "Continuing without this item's prices.",
                                    item.getItemId()));
                            default -> log.error(String.format("getCollection(collectionId, returnPrice): " +
                                            "unexpected response from price-updater (itemId=%d). " +
                                            "Continuing without this item's prices.",
                                    item.getItemId()));
                        }

                    } catch (Exception e) {
                        log.error(String.format("getCollection(collectionId, returnPrice): could not get response from " +
                                "price-updater (itemId=%d). Continuing without this item's prices.", item.getItemId()), e);
                    }
                }
            }

            if(collection.getPriceTotal().isEmpty())
                collection.setPriceTotal(null);
            asyncResponse.complete(Response.status(Response.Status.OK).entity(collection).build());
        });

        return asyncResponse;
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
                    responseCode = "405",
                    description = "Modifying collection is not allowed"
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

        if(collection == null || (collection.getCollectionName() != null
                && collection.getCollectionName().equals("")))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            if(collectionBean.isCollectionLocked(collectionId))
                return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
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
                    responseCode = "405",
                    description = "Modifying collection is not allowed"
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

        try {
           if(collectionBean.isCollectionLocked(collectionId))
               return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

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
                    description = "Collection or item not found"
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
    public CompletionStage<Response> createCollectionItem(@RequestBody(
            description = "Item DTO (itemId, amount). Default amount is 1.", required = true,
            content = @Content(schema = @Schema(implementation = ItemDTO.class))) ItemDTO item,
                                                          @Parameter(name = "collection ID", required = true)
                                                          @PathParam("collectionId") Integer collectionId) {

        // Create new async response, check that itemId is provided and
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
        if(item == null || item.getItemId() == null) {
            asyncResponse.complete(Response.status(Response.Status.BAD_REQUEST).build());
            return asyncResponse;
        }

        if(item.getAmount() == null)
            item.setAmount(1);

        // Check that item is not already added
        boolean resourceExists = true;
        try {
            collectionItemEntityBean.getCollectionItemEntity(new CollectionItemKey(collectionId, item.getItemId()));
        } catch (NotFoundException e) {
            resourceExists = false;
        }
        if(resourceExists) {
            asyncResponse.complete(Response.status(Response.Status.CONFLICT).build());
            return asyncResponse;
        }

        if(apiProperties.getVerifyItemExists()) {
            // create async request to price updater
            Future<Response> updaterRequest = priceUpdaterClient.getItemRequest(item.getItemId(), false).async().get();

            executor.execute( () -> {
                // Asynchronously check if item is already in collection; if so, return CONFLICT
                try {
                    // Try to retrieve response from price updater and check its status
                    Response updaterResponse = updaterRequest.get();
                    switch (updaterResponse.getStatus()) {
                        // If status is OK, try to add item to collection
                        case 200 -> {
                            try {
                                collectionItemEntityBean.createCollectionItemEntity(collectionId, item.getItemId(), item.getAmount());
                            } catch (NotFoundException e) {
                                asyncResponse.complete(Response.status(Response.Status.NOT_FOUND).build());
                                return;
                            } catch (RuntimeException e) {
                                asyncResponse.complete(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
                                return;
                            }
                            asyncResponse.complete(Response.status(Response.Status.NO_CONTENT).build());
                        }
                        // If status is NOT FOUND, return this status
                        case 404 -> asyncResponse.complete(Response.status(Response.Status.NOT_FOUND).build());
                        // If status is something else, return generic error
                        default -> {
                            log.error("createCollectionItem(item, collectionId): unexpected response from price-updater.");
                            asyncResponse.complete(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
                        }
                    }
                } catch(Exception e) {
                    // In case response cannot be retrieved, return generic error
                    log.error("createCollectionItem(item, collectionId): could not get response from price-updater.", e);
                    asyncResponse.complete(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
                }
            });

        } else {
            // Just persist collection item
            try {
                collectionItemEntityBean.createCollectionItemEntity(collectionId, item.getItemId(), item.getAmount());
            } catch (NotFoundException e) {
                asyncResponse.complete(Response.status(Response.Status.NOT_FOUND).build());
                return asyncResponse;
            } catch (RuntimeException e) {
                asyncResponse.complete(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
                return asyncResponse;
            }

            asyncResponse.complete(Response.status(Response.Status.NO_CONTENT).build());
        }

        return asyncResponse;
    }

    @Operation(description = "Update amount of item in collection.", summary = "Update collection item")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Collection item amount successfully updated"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Collection item not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error in persisting collection item"
            )
    })
    @PUT
    @Path("{collectionId}/{itemId}")
    public Response putCollectionItem(@RequestBody(description = "Item DTO (amount only).", required = true,
            content = @Content(schema = @Schema(implementation = ItemDTO.class))) ItemDTO itemDTO,
                                      @Parameter(name = "collection ID", required = true)
                                      @PathParam("collectionId") Integer collectionId,
                                      @Parameter(name = "item ID", required = true)
                                          @PathParam("itemId") Integer itemId) {

        if(itemDTO == null || itemDTO.getAmount() == null || itemDTO.getAmount() < 1)
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            collectionItemEntityBean.putCollectionItemEntity(collectionId, itemId, itemDTO.getAmount());
        } catch(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch(RuntimeException e) {
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
