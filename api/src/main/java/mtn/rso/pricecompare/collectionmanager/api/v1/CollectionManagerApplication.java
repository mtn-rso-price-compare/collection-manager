package mtn.rso.pricecompare.collectionmanager.api.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(title = "Collection manager API", version = "v1",
        contact = @Contact(email = "mr0017@student.uni-lj.si"),
        license = @License(name = "dev"), description = "API for creating and managing collections of items."),
        servers = @Server(url = "http://localhost:8080/"))
@ApplicationPath("/v1")
public class CollectionManagerApplication extends Application {

}
