package mtn.rso.pricecompare.priceupdater.api.v1.resources;

import mtn.rso.pricecompare.priceupdater.lib.Request;
import mtn.rso.pricecompare.priceupdater.services.beans.RequestBean;
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
@Path("/request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RequestResource {

    private Logger log = Logger.getLogger(RequestResource.class.getName());

    @Inject
    private RequestBean requestBean;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get log of all recent requests to update prices.", summary = "Get all requests")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of requests",
                    content = @Content(schema = @Schema(implementation = Request.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getRequest() {

        List<Request> requests = requestBean.getRequestFilter(uriInfo);
        return Response.status(Response.Status.OK).entity(requests).build();
    }

    @Operation(description = "Make a new request to update prices.", summary = "Make request")
    @APIResponses({
            @APIResponse(responseCode = "202",
                    description = "Request accepted for processing",
                    content = @Content(schema = @Schema(implementation = Request.class))
            ),
            @APIResponse(responseCode = "500",
                    description = "Error in persisting request"
            )})
    @POST
    public Response createRequest() {

        Request request;
        try {
            request = requestBean.createRequest();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(request).build();
    }

    @Operation(description = "Get information for a recent request to update prices.", summary = "Get information for a request")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Request",
                    content = @Content(schema = @Schema(implementation = Request.class))
            ),
            @APIResponse(responseCode = "404",
                    description = "Request not found"
            )})
    @GET
    @Path("/{requestId}")
    public Response getRequest(@Parameter(description = "Request ID", required = true)
                               @PathParam("requestId") Integer requestId) {

        Request request;
        try {
            request = requestBean.getRequest(requestId);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(request).build();
    }

}
