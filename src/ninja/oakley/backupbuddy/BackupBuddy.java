package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ninja.oakley.backupbuddy.configuration.ConfigurationManager;
import ninja.oakley.backupbuddy.configuration.LoadConfigurationRunnable;
import ninja.oakley.backupbuddy.controllers.AddBucketScreenController;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;
import ninja.oakley.backupbuddy.controllers.ContextMenuScreenController;
import ninja.oakley.backupbuddy.controllers.KeyManagerScreenController;
import ninja.oakley.backupbuddy.controllers.QueueScreenController;
import ninja.oakley.backupbuddy.encryption.EncryptionManager;
import ninja.oakley.backupbuddy.project.ProjectController;
import ninja.oakley.backupbuddy.queue.RequestHandler;
import ninja.oakley.backupbuddy.queue.UploadRequest;

/**
 * Application used to upload and download large to small amount of files to and
 * from the Google Cloud Storage Platform. JavaFX interface allows for easy
 * selection of files to be uploaded and to choose the destination of files
 * downloaded Authentication is done using a JSON key accessed from the Google
 * Dev Console
 *
 *
 * @author Griffin Dunn
 *
 */
public class BackupBuddy extends Application {

    private static final Logger logger = LogManager.getLogger(BackupBuddy.class);
    private static final String APPLICATION_NAME = "Backup Buddy/1.4";

    private volatile ConcurrentHashMap<String, ProjectController> accounts = new ConcurrentHashMap<String, ProjectController>();

    private ConfigurationManager configurationManager;
    private RequestHandler requestHandler;
    private EncryptionManager encryptionManager;

    private Stage primaryStage;
    private Stage secondaryStage;
    private Stage queueStage;

    private BaseScreenController baseScreenController;
    private AddBucketScreenController addBucketController;
    private QueueScreenController queueScreenController;
    private ContextMenuScreenController contextMenuController;
    private KeyManagerScreenController keyManagerController;

    /**
     * Initializes the Application and loads various configuration files and
     * components
     *
     * (non-Javadoc)
     *
     * @see javafx.application.Application#init()
     */
    @Override
    public void init() {

        encryptionManager = new EncryptionManager(this);
        configurationManager = new ConfigurationManager();
        requestHandler = new RequestHandler(this);
        
        new Thread(new LoadConfigurationRunnable(this)).start();

        requestHandler.createThreads(requestHandler.getMaxThreads(), false);

        try {
            addBucketController = new AddBucketScreenController(this);
            addBucketController.load();

            queueScreenController = new QueueScreenController(this);
            queueScreenController.load();

            contextMenuController = new ContextMenuScreenController(this);
            contextMenuController.load();

            keyManagerController = new KeyManagerScreenController(this);
            keyManagerController.load();

            baseScreenController = new BaseScreenController(this);
            baseScreenController.load();

        } catch (IOException e) {
            logger.error("Failed to load FXML file: " + e);
        }
    }

    /**
     * Starts the application and opens the primary window
     *
     * (non-Javadoc)
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        primaryStage.setTitle(APPLICATION_NAME);

        Scene scene = new Scene(baseScreenController.getBase());
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                String bucket = getBaseController().getCurrentBucket();
                ProjectController controller = getBaseController().getSelectedProjectController();
                if (!Strings.isNullOrEmpty(bucket) && controller != null && db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        UploadRequest req = new UploadRequest(controller, file.toPath(), bucket);
                        getRequestHandler().addRequest(req);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

    }

    /**
     * Run on stop
     */
    @Override
    public void stop() {
        requestHandler.stopAllThreads();
    }

    /**
     * Method initiated on start Starts the Application Initialization
     *
     * @param args
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * HashMap used to
     *
     * @return list of BucketManagers
     */
    public ConcurrentHashMap<String, ProjectController> getProjects() {
        return accounts;
    }

    /**
     * Used to control the Add Bucket Window
     *
     * @return bucket controller
     */
    public AddBucketScreenController getAddBucketController() {
        return addBucketController;
    }

    /**
     * Used to control the Base Window
     *
     * @return base controller
     */
    public KeyManagerScreenController getKeyManagerController() {
        return keyManagerController;
    }

    /**
     * Used to control the Base Window
     *
     * @return base controller
     */
    public BaseScreenController getBaseController() {
        return baseScreenController;
    }

    /**
     * Used to control the queue window
     *
     * @return queue controller
     */
    public QueueScreenController getQueueController() {
        return queueScreenController;
    }

    public ContextMenuScreenController getContextMenuController() {
        return contextMenuController;
    }

    /**
     * Get a secondary stage, which initializes lazily Used for screens that
     * gather information
     *
     * @return secondary stage
     */
    public Stage getSecondaryStage() {
        if (secondaryStage == null) {
            secondaryStage = new Stage();
            secondaryStage.setResizable(false);
        }

        return secondaryStage;
    }

    /**
     * Gets the primary stage which everything is based off of
     *
     * @return primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Get the queue stage which shows the progress of requests
     *
     * @return queue stage
     */
    public Stage getQueueStage() {
        if (queueStage == null) {
            queueStage = new Stage();
            queueStage.setResizable(false);
            queueStage.initStyle(StageStyle.UTILITY);
            queueStage.setAlwaysOnTop(false);
        }

        return queueStage;
    }

    /**
     * Get the configuration manager used save and load projects for use in the
     * program
     *
     * @return configuration manager
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Get the request manager used to upload and download files
     *
     * @return request manager
     */
    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    /**
     * Get the encryption manager to encrypt and decrypt files. Also used to
     * load private keys.
     *
     * @return encryption manager
     */
    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }
}
