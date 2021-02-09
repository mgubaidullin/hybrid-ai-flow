package one.entropy.haf.classificator;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.translate.Translator;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static one.entropy.haf.classificator.ModelUtils.MODEL_NAME;

@ApplicationScoped
public class HafInference extends EndpointRouteBuilder {

    @ConfigProperty(name = "model.folder")
    String modelFolder;

    public void configure() throws Exception {
        errorHandler(deadLetterChannel(log(".").getUri()).logExhausted(true)
                .useOriginalMessage().maximumRedeliveries(3).redeliveryDelay(1000));

        restConfiguration()
                .component("netty-http")
                .port(8080)
                .contextPath("api")
                .apiContextPath("api-doc")
                .apiVendorExtension(true)
                .apiProperty("api.title", "Hybrid AI Classification API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        rest()
                .get("/classes/{filename}")
                .id("classify")
                .produces("application/json")
                .to("direct:classify");

        from(direct("classify"))
                .pollEnrich().simple(file("{{camel.file.data.folder}}").fileName("${header.filename}").noop(true).idempotent(false).getUri()).timeout(3000)
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

    @Named("MyModel")
    Model createLocalModel() throws IOException, MalformedModelException {
//         create deep learning model
        Model model = ModelUtils.createModel();
        model.load(Paths.get(modelFolder), MODEL_NAME);
        return model;
    }

    @Named("MyTranslator")
    Translator createTranslator() {
//         create translator for pre-processing and postprocessing
        return ModelUtils.createTranslator();
    }
}
