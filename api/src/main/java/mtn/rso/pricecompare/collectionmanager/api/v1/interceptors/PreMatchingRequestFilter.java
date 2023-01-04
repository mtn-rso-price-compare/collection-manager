package mtn.rso.pricecompare.collectionmanager.api.v1.interceptors;

import org.apache.logging.log4j.ThreadContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class PreMatchingRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        MultivaluedMap<String, String> headers = ctx.getHeaders();
        if(headers.containsKey("uniqueRequestId"))
            ThreadContext.put("uniqueRequestId", headers.getFirst("uniqueRequestId"));

    }
}
