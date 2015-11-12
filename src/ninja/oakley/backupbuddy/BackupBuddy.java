package ninja.oakley.backupbuddy;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import ninja.oakley.backupbuddy.controllers.AddBucketScreenController;
import ninja.oakley.backupbuddy.controllers.AddProjectScreenController;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application used to upload and download large to small amount of files to and from the Google Cloud Storage Platform.
 * JavaFX interface allows for easy selection of files to be uploaded and to choose the destination of files downloaded
 * Authentication is done using a JSON key accessed from the Google Dev Console
 * 
 * 
 * @author Griffin Dunn
 *
 */
public class BackupBuddy extends Application {

	private static final Logger logger = LogManager.getLogger(BackupBuddy.class);
	private static final String APPLICATION_NAME = "Backup Buddy/1.3";

	private volatile ConcurrentHashMap<String, BucketManager> accounts = new ConcurrentHashMap<String, BucketManager>();
	
	private ConfigurationManager configurationManager;
	private RequestManager requestManager;

	private Stage primaryStage;
	private Stage secondaryStage;
	//private Stage queueStage;
	
	private BaseScreenController baseScreenController;
	private AnchorPane baseAnchorPane;

	private AddProjectScreenController addProjectController;
	public Pane addProjectPane;

	private AddBucketScreenController addBucketController;
	public Pane addBucketPane;

	/**
	 * Initializes the Application and loads various configuration files and components
	 * 
	 * (non-Javadoc)
	 * @see javafx.application.Application#init()
	 */
	@Override
	public void init() {		
		try {
			FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "Base.fxml");
			baseScreenController = new BaseScreenController(this);
			setController(baseLoader, baseScreenController);
			baseAnchorPane = (AnchorPane) baseLoader.load();

			FXMLLoader addProjectLoader = loadFxmlFile(AddProjectScreenController.class, "AddProject.fxml");
			addProjectController = new AddProjectScreenController(this);
			setController(addProjectLoader, addProjectController);
			addProjectPane = (Pane) addProjectLoader.load();

			FXMLLoader addBucketLoader = loadFxmlFile(AddBucketScreenController.class, "AddBucket.fxml");
			addBucketController = new AddBucketScreenController(this);
			setController(addBucketLoader, addBucketController);
			addBucketPane = (Pane) addBucketLoader.load();

		} catch (IOException e) {
			logger.error("Failed to load FXML file: " + e);
		}
		
		configurationManager = new ConfigurationManager(this);
		new Thread(new LoadConfigurationRunnable(this)).start();
		
		requestManager = new RequestManager(this);
		requestManager.createThreads(requestManager.getMaxThreads(), false);
	}

	/**
	 * Starts the application and opens the primary window
	 * 
	 * (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setResizable(false);
		primaryStage.setTitle(APPLICATION_NAME);
		
		Scene scene = new Scene(baseAnchorPane);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/**
	 * Run on stop
	 */
	@Override
	public void stop(){
		requestManager.stopAllThreads();
	}

	/**
	 * Method initiated on start
	 * Starts the Application Initialization
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
	public ConcurrentHashMap<String, BucketManager> getProjects() {
		return this.accounts;
	}

	/**
	 * Used to control the Add Project Window
	 * 
	 * @return Project Controller
	 */
	public AddProjectScreenController getAddProjectController() {
		return this.addProjectController;
	}
	
	/**
	 * Used to control the Add Bucket Window
	 * 
	 * @return bucket controller
	 */
	public AddBucketScreenController getAddBucketController() {
		return this.addBucketController;
	}
	
	/**
	 * Used to control the Base Window
	 * 
	 * @return base controller
	 */
	public BaseScreenController getBaseController() {
		return this.baseScreenController;
	}

	/**
	 * Get a secondary stage, which initializes lazily
	 * Used for screens that gather information
	 * 
	 * @return secondary stage
	 */
	public Stage getSecondaryStage() {
		if(secondaryStage == null) {
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
	 * Get the FXMLLoader for a specific file
	 * 
	 * @param clazz class you want to search from
	 * @param name of the file
	 * @return FXMLLoader loaded from file
	 * @throws IOException
	 */
	private static FXMLLoader loadFxmlFile(Class<?> clazz, String name) throws IOException {
		return new FXMLLoader(clazz.getResource(name));
	}

	/**
	 * Allows you to initialize your own controller for an FXML file
	 * 
	 * @return loader with the controller assigned
	 */
	private static FXMLLoader setController(FXMLLoader loader, Object obj){
		loader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> paramClass) {
				return obj;
			}
		});
		return loader;
	}
	
	/**
	 * Get the configuration manager used save and load projects for use in the program
	 * 
	 * @return configuration manager
	 */
	public ConfigurationManager getConfigurationManager(){
		return this.configurationManager;
	}
	
	/**
	 * Get the request manager used to upload and download files
	 * 
	 * @return request manager
	 */
	public RequestManager getRequestManager(){
		return this.requestManager;
	}
}
