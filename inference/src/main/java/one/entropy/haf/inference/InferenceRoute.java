package one.entropy.haf.inference;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class InferenceRoute extends EndpointRouteBuilder {

    private static final Logger LOG = Logger.getLogger(InferenceRoute.class);

    public void configure() throws Exception {
        from(direct("classify"))
                .pollEnrich().simple("file:{{camel.file.data.folder}}?fileName=${header.filename}&noop=true&idempotent=false").timeout(3000)
                .convertBodyTo(byte[].class)
                .to(djl("cv/image_classification").model("MyModel").translator("MyTranslator"))
                .process(exchange -> {
                    Map result = exchange.getIn().getBody(Map.class);
                    result.put("NEGATIVE", result.remove("0"));
                    result.put("POSITIVE", result.remove("1"));
                    exchange.getIn().setBody(result);
                })
                .marshal().json();
    }
}
