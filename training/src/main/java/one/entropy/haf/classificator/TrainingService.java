package one.entropy.haf.classificator;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.ImageFolder;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class TrainingService {

    private static final Logger LOG = Logger.getLogger(TrainingService.class);

    @ConfigProperty(name = "model.folder")
    String modelFolder;

    @ConfigProperty(name = "data.folder",  defaultValue = "data")
    String dataFolder;

    @ConfigProperty(name = "data.url")
    String dataUrl;

    @ConfigProperty(name = "model.bucket")
    String bucket;

    @Inject
    S3Client s3;

    public static final String MODEL_NAME = "defects";
    public static final int IMAGE_HEIGHT = 16;
    public static final int IMAGE_WIDTH = 16;
    public static final int NUM_CLASSES = 2;
    public static final int BATCH_SIZE = 64;
    public static final int EPOCH = 1;

    public Path train() throws IOException, TranslateException {
        downloadDataset();
        Path modelFileName = createNeuralNet();
        deleteDataset();
        return modelFileName;
    }

    private Path createNeuralNet() throws IOException, TranslateException {
        LOG.info("Creating NeuralNet");
        // Construct neural network
        Block resNet =  ResNetV1.builder()
                .setImageShape(new Shape(3, IMAGE_HEIGHT, IMAGE_WIDTH))
                .setNumLayers(20)
                .setOutSize(NUM_CLASSES)
                .build();
        try (Model model = Model.newInstance(MODEL_NAME)) {
            model.setBlock(resNet);

            // get training and validation dataset
            Repository repository = Repository.newInstance(MODEL_NAME, Paths.get(dataFolder));
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
                EasyTrain.fit(trainer, EPOCH, trainingSet, validateSet);
            }
            model.save(Paths.get(modelFolder), MODEL_NAME);
        }
        String fileName = String.format(Locale.ENGLISH, "%s-%04d.params", MODEL_NAME, EPOCH);
        return Paths.get(modelFolder).resolve(fileName);
    }

    private void downloadDataset() {
        LOG.info("Downloading dataset");
        try {
            if (!Files.exists(Paths.get(dataFolder))) {
                Files.createDirectory(Paths.get(dataFolder));
            }
            URL url = new URL(dataUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(conn.getInputStream());
            ZipInputStream inputStream = new ZipInputStream(bufferedInputStream);

            for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
                if (entry.isDirectory()){
                    System.out.println(entry.getName());
                }
                StringBuilder pathBuilder = new StringBuilder(dataFolder).append('/').append(entry.getName());
                File file = new File(pathBuilder.toString());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else if (!entry.getName().startsWith("_")) {
                    if (!Files.exists(Paths.get(file.getParent()))) {
                        Files.createDirectory(Paths.get(file.getParent()));
                    }
                    extractFile(inputStream, file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private void deleteDataset() throws IOException {
        LOG.info("Deleting dataset");
        Files.walk(Paths.get(dataFolder))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public void store(java.nio.file.Path modelFile)  {
        LOG.info("Storing model in S3");
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(modelFile.getFileName().toString())
                .contentType("application/binary")
                .build();
        PutObjectResponse response = s3.putObject(request, modelFile);
        LOG.info(response.toString());
        LOG.info("Stored model in S3");
    }
}
