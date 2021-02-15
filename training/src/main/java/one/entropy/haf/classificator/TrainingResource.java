package one.entropy.haf.classificator;

import ai.djl.translate.TranslateException;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;

@Path("/train")
public class TrainingResource {

    private static final Logger LOG = Logger.getLogger(TrainingService.class);

    @Inject
    EventBus bus;

    @Inject
    TrainingService training;

    @POST
    public void train() {
        bus.publish("train", null);
    }

    @ConsumeEvent(value = "train")
    @Blocking
    void consumeBlocking(String message) throws IOException, TranslateException {
        java.nio.file.Path model = training.train();
        training.store(model);
    }



}