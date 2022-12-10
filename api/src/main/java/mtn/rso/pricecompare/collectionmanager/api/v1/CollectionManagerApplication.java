package mtn.rso.pricecompare.collectionmanager.api.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Collection manager API",
                version = "v1",
                description = "API for creating and managing collections of items.",
                contact = @Contact(
                        name = "Mihael Rajh",
                        email = "mr0017@student.uni-lj.si"
                )
        ), servers = {@Server(url = "http://localhost:8080/")},
        externalDocs = @ExternalDocumentation(
                url = "https://github.com/mtn-rso-price-compare/docs",
                description = "Slovene documentation of the application that this API is a part of."
        )
)
@ApplicationPath("/v1")
public class CollectionManagerApplication extends Application {

}
