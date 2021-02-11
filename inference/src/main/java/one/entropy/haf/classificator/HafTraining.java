package one.entropy.haf.classificator;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.ImageFolder;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.Repository;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static one.entropy.haf.classificator.ModelUtils.*;

public class HafTraining {

    private static final String TRAINING_DATA = "https://github.com/mgubaidullin/hybrid-ai-flow/releases/download/dataset/training.zip";
    private static final Path PATH = Paths.get(DATA_DIR);

    private HafTraining() {
        // No-op; won't be called
    }

    public static void main(String[] args) throws IOException, TranslateException {
        downloadDataset();
        createNeuralNet();
        deleteDataset();
    }

    private static void createNeuralNet() throws IOException, TranslateException {
        // Construct neural network
        try (Model model = ModelUtils.createModel()) {

            // get training and validation dataset
            Repository repository = Repository.newInstance(MODEL_NAME, DATA_DIR);
            ImageFolder dataset = ImageFolder.builder()
                    .setRepository(repository)
                    .optPipeline(new Pipeline().add(new Resize(IMAGE_WIDTH, IMAGE_HEIGHT)).add(new ToTensor()))
                    .setSampling(BATCH_SIZE, true).build();
            dataset.prepare(new ProgressBar());
            RandomAccessDataset[] sets = dataset.randomSplit(75, 25);

            RandomAccessDataset trainingSet = sets[0];
            RandomAccessDataset validateSet = sets[1];

            // setup training configuration
            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .addEvaluator(new Accuracy()).optDevices(Device.getDevices(Device.getGpuCount()))
                    .addTrainingListeners(TrainingListener.Defaults.logging());

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                Shape inputShape = new Shape(1, 3, IMAGE_HEIGHT, IMAGE_HEIGHT);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);
                EasyTrain.fit(trainer, 2, trainingSet, validateSet);
            }
            model.save(Paths.get(MODEL_DIR), MODEL_NAME);
        }
    }

    private static void downloadDataset() {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectory(PATH);
            }
            URL url = new URL(TRAINING_DATA);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(conn.getInputStream());
            ZipInputStream inputStream = new ZipInputStream(bufferedInputStream);

            for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
                StringBuilder pathBuilder = new StringBuilder(PATH.toString()).append('/').append(entry.getName());
                File file = new File(pathBuilder.toString());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else if (!entry.getName().startsWith("_")) {
                    extractFile(inputStream, file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private static void deleteDataset() throws IOException {
        Files.delete(PATH);
    }
}
