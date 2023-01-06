package mtn.rso.pricecompare.collectionmanager.services.clients;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import mtn.rso.pricecompare.collectionmanager.lib.ItemDTO;
import mtn.rso.pricecompare.collectionmanager.services.config.ClientProperties;
import org.apache.logging.log4j.ThreadContext;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.annotation.processing.Completion;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.*;

@ApplicationScoped
public class PriceUpdaterClient {

    @Inject
    ClientProperties clientProperties;

    private WebTarget getBaseWebTarget() {

        return ClientBuilder.newClient()
                .target("http://" + clientProperties.getPriceUpdaterHost() +
                        ":" + clientProperties.getPriceUpdaterPort() + "/");
    }

    private Invocation.Builder getItemRequest(Integer itemId, Boolean returnPrice, String requestId) {

        Invocation.Builder request = getBaseWebTarget().path("/v1/item/{itemId}")
                .resolveTemplate("itemId", Integer.toString(itemId))
                .queryParam("returnPrice", returnPrice)
                .request();

        if(requestId != null)
            request = request.header("uniqueRequestId", requestId);
        else if(ThreadContext.containsKey("uniqueRequestId"))
            request = request.header("uniqueRequestId", ThreadContext.get("uniqueRequestId"));
        return request;
    }

    @Counted(name = "get_item_counter", description = "Displays the total number of getItem(itemId, returnPrice, requestId) invocations that have occurred.")
    public CompletionStage<ItemDTO> getItem(Integer itemId, Boolean returnPrice, String requestId, Executor executor) {

        CompletableFuture<ItemDTO> asyncResponse = new CompletableFuture<>();
        executor.execute(() -> getItemCallback(getItemRequest(itemId, returnPrice, requestId), asyncResponse));
        return asyncResponse;
    }

    @CircuitBreaker(requestVolumeThreshold = 3, delay = 30, delayUnit  = ChronoUnit.SECONDS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getItemFallback")
    public void getItemCallback(Invocation.Builder request,  CompletableFuture<ItemDTO> asyncResponse) {
        Response response = request.get();

        switch (response.getStatus()) {

            // If status is OK, return item
            case 200 -> {
                ItemDTO receivedItem = response.readEntity(ItemDTO.class);
                asyncResponse.complete(receivedItem);
            }

            // If status is NOT FOUND, return NotFoundException
            case 404 -> asyncResponse.completeExceptionally(new NotFoundException());

            // If status is something else, return application error
            default -> asyncResponse.completeExceptionally(new WebApplicationException(response.getStatus()));
        }
    }

    public void getItemFallback(Invocation.Builder request,  CompletableFuture<ItemDTO> asyncResponse) {
        // If request fails, return generic error
        asyncResponse.completeExceptionally(new ProcessingException("PriceUpdaterClient: Fallback mechanism triggered."));
    }
}
