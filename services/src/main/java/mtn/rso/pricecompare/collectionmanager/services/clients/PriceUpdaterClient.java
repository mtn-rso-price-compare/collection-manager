package mtn.rso.pricecompare.collectionmanager.services.clients;

import mtn.rso.pricecompare.collectionmanager.services.config.ClientProperties;
import org.apache.logging.log4j.ThreadContext;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

@RequestScoped
public class PriceUpdaterClient {

    @Inject
    ClientProperties clientProperties;

    private WebTarget getBaseWebTarget() {

        return ClientBuilder.newClient()
                .target("http://" + clientProperties.getPriceUpdaterHost() +
                ":" + clientProperties.getPriceUpdaterPort() + "/");
    }

    public Invocation.Builder getItemRequest(Integer itemId, Boolean returnPrice) {

        Invocation.Builder request = getBaseWebTarget().path("/v1/item/{itemId}")
                .resolveTemplate("itemId", Integer.toString(itemId))
                .queryParam("returnPrice", returnPrice)
                .request();

        if(ThreadContext.containsKey("uniqueRequestId"))
            request = request.header("uniqueRequestId", ThreadContext.get("uniqueRequestId"));
        return request;
    }
}
