package one.entropy.haf.classificator;

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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModelUtils {

    public static final String MODEL_DIR = "models";
    public static final String DATA_DIR = "dataset/training";
    public static final String MODEL_NAME = "defects";
    public static final int IMAGE_HEIGHT = 23;
    public static final int IMAGE_WIDTH = 23;
    public static final int NUM_CLASSES = 2;
    public static final int BATCH_SIZE = 64;

    public static final Block resNet50 =  ResNetV1.builder()
            .setImageShape(new Shape(3, IMAGE_HEIGHT, IMAGE_WIDTH))
            .setNumLayers(20)
            .setOutSize(NUM_CLASSES)
            .build();

    public static Model createModel(){
        Model model = Model.newInstance(MODEL_NAME);
        model.setBlock(resNet50);
        return model;
    }

    public static Translator<Image, Classifications> createTranslator(){
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
