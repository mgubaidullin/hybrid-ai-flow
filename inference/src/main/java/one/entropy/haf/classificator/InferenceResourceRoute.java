package one.entropy.haf.classificator;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InferenceResourceRoute extends EndpointRouteBuilder {

    public void configure() throws Exception {
        restConfiguration()
                .component("netty-http")
                .port(8080)
                .apiContextPath("api-doc")
                .apiVendorExtension(true)
                .apiProperty("api.title", "Hybrid AI Classification API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        rest()
                .get("/class/{filename}")
                .id("classify")
                .produces("application/json")
                .to("direct:classify");
    }
}
