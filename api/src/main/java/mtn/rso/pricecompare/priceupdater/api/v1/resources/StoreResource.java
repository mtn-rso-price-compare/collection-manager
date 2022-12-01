package mtn.rso.pricecompare.priceupdater.api.v1.resources;

import mtn.rso.pricecompare.priceupdater.lib.Store;
import mtn.rso.pricecompare.priceupdater.services.beans.StoreBean;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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
@Path("/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {

    private Logger log = Logger.getLogger(StoreResource.class.getName());

    @Inject
    private StoreBean storeBean;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get a list of all stores.", summary = "Get all stores")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of stores",
                    content = @Content(schema = @Schema(implementation = Store.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getStore() {

        List<Store> storeList = storeBean.getStoreFilter(uriInfo);
        return Response.status(Response.Status.OK).entity(storeList).build();
    }


    @Operation(description = "Get a store and its prices.", summary = "Get store prices")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Store",
                    content = @Content(schema = @Schema(implementation = Store.class))
            ),
            @APIResponse(responseCode = "404",
                    description = "Store not found"
            )})
    @GET
    @Path("/{storeId}")
    public Response getStore(@Parameter(description = "Store ID", required = true)
                                     @PathParam("storeId") Integer storeId) {

        Store store;
        try {
            store = storeBean.getStore(storeId);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(store).build();
    }

}
