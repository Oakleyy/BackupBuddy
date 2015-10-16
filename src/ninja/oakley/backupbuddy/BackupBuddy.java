package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import ninja.oakley.backupbuddy.controllers.AddBucketScreenController;
import ninja.oakley.backupbuddy.controllers.AddProjectScreenController;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Small text-file uploading program. Uploads the data to a "bucket" on the Google Cloud servers.
 * The program is run by using the command line. You must have a Google Storage Bucket setup and 
 * have the key 
 * 
 * @author Griffin Dunn
 *
 */
public class BackupBuddy extends Application {

	private static final Logger logger = LogManager.getLogger("BackupBuddy");
	private static final String APPLICATION_NAME = "Backup Buddy/1.2";

	private ConcurrentHashMap<String, BucketManager> accounts = new ConcurrentHashMap<String, BucketManager>();

	private BaseScreenController baseScreenController;
	private AnchorPane baseAnchorPane;

	private Stage primaryStage;
	private Stage secondaryStage;

	private AddProjectScreenController addProjectController;
	public Pane addProjectPane;
	
	private AddBucketScreenController addBucketController;
	public Pane addBucketPane;

	public ConcurrentHashMap<String, BucketManager> getProjects() {
		return this.accounts;
	}

	public AddProjectScreenController getAddProjectController() {
		return this.addProjectController;
	}

	public BaseScreenController getBaseController() {
		return this.baseScreenController;
	}

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
	}

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
	 * Method initiated on start
	 * Starts the Application Initilization
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	public static void sendActionMessage(Text target, String st, Color color){
		target.setText(st);
		target.setFill(color);
	}

	public static FXMLLoader loadFxmlFile(Class<?> clazz, String name) throws IOException {
		return new FXMLLoader(clazz.getResource(name));
	}

	public Stage getSecondaryStage() {
		if(secondaryStage == null) {
			secondaryStage = new Stage();
			secondaryStage.setResizable(false);
		}

		return secondaryStage;
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	private static void setController(FXMLLoader loader, Object obj){
		loader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> paramClass) {
				return obj;
			}
		});
	}
	
	public static void configureFileChooser(final FileChooser fileChooser) {      
		fileChooser.setTitle("Select...");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}
}
