package one.entropy.haf.inference;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.translate.Translator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class ModelConfiguration {

    public static final String MODEL_NAME = "defects";
    public static final int IMAGE_HEIGHT = 23;
    public static final int IMAGE_WIDTH = 23;
    public static final int NUM_CLASSES = 2;

    @ConfigProperty(name = "model.folder")
    String modelFolder;

    public static final Block resNet =  ResNetV1.builder()
            .setImageShape(new Shape(3, IMAGE_HEIGHT, IMAGE_WIDTH))
            .setNumLayers(20)
            .setOutSize(NUM_CLASSES)
            .build();

    @Named("MyModel")
    Model createLocalModel() throws IOException, MalformedModelException {
//         create deep learning model
        Model model = Model.newInstance(MODEL_NAME);
        model.setBlock(resNet);
        model.load(Paths.get(modelFolder), MODEL_NAME);
        return model;
    }

    @Named("MyTranslator")
    Translator<Image, Classifications> createTranslator() {
//         create translator for pre-processing and postprocessing
        List<String> classes = IntStream.range(0, 2).mapToObj(String::valueOf).collect(Collectors.toList());
        Translator<Image, Classifications> translator
                = ImageClassificationTranslator.builder()
                .addTransform(new Resize(IMAGE_WIDTH, IMAGE_HEIGHT))
                .addTransform(new ToTensor())
                .optApplySoftmax(true)
                .optSynset(classes)
                .build();
        return translator;
    }
}
