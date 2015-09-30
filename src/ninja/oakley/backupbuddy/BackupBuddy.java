package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;


/**
 * Small text-file uploading program. Uploads the data to a "bucket" on the Google Cloud servers.
 * The program is run by using the command line. You must have a Google Storage Bucket setup and 
 * have the key 
 * 
 * @author Griffin Dunn
 *
 */
public class BackupBuddy extends Application {

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final Logger logger = LogManager.getLogger("BackupBuddy");
	private static final String APPLICATION_NAME = "Backup Buddy/1.0";

	public static Storage storageService;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setResizable(false);
		primaryStage.setTitle(APPLICATION_NAME);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Scene scene = new Scene(grid, 300, 275);
		primaryStage.setScene(scene);

		Text title = new Text("Lets backup!");
		title.setFont(Font.font("Corrier New", FontWeight.NORMAL, 20));

		/*
		 * File
		 */
		Label filePathLabel = new Label("File:");
		grid.add(filePathLabel, 0, 1);

		TextField filePath = new TextField();
		grid.add(filePath, 1, 1);

		final FileChooser fileChooser = new FileChooser();
		Button fileSelButton = new Button("...");
		grid.add(fileSelButton, 2, 1);

		fileSelButton.setOnAction(
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent e) {
						configureFileChooser(fileChooser);
						File file = fileChooser.showOpenDialog(primaryStage);
						if (file != null) {
							filePath.setText(file.getAbsolutePath());
						}
					}
				});

		/*
		 * Bucket
		 */
		 Label bucketNameLabel = new Label("Bucket:");
		 grid.add(bucketNameLabel, 0, 2);

		 TextField bucketName = new TextField();
		 grid.add(bucketName, 1, 2);

		 /*
		  * Keyfile
		  */
		 Label keyFilePathLabel = new Label("Keyfile:");
		 grid.add(keyFilePathLabel, 0, 3);

		 TextField keyFilePath = new TextField();
		 grid.add(keyFilePath, 1, 3);

		 Button keyFileSelButton = new Button("...");
		 grid.add(keyFileSelButton, 2, 3);
		 
		 keyFileSelButton.setOnAction(
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(final ActionEvent e) {
							configureFileChooser(fileChooser);
							File file = fileChooser.showOpenDialog(primaryStage);
							if (file != null) {
								keyFilePath.setText(file.getAbsolutePath());
							}
						}
					});

		 final Text actiontarget = new Text();
		 grid.add(actiontarget, 1, 6);

		 Button btn = new Button("Upload");
		 HBox hbBtn = new HBox(10);
		 hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		 hbBtn.getChildren().add(btn);
		 grid.add(hbBtn, 1, 4);

		 btn.setOnAction(new EventHandler<ActionEvent>() {

			 @Override
			 public void handle(ActionEvent event) {
				 String file = filePath.getText();
				 String keyFile = keyFilePath.getText();
				 String bucket = bucketName.getText();

				 if(file.isEmpty() || keyFile.isEmpty() || bucket.isEmpty()){
					 actiontarget.setText("One or more of the fields are blank.");
					 event.consume();
				 }

				 /*
				  * Attempt to find the JSON key file and throw an error if not found
				  * If found, authenticate with the Google servers
				  */
				 try {
					 File key = new File(keyFile);
					 storageService = retrieveStorageService(key);
					 logger.info("Credentials accepted! Storage service retrieved.");
				 } catch (GeneralSecurityException e1) {
					 logger.error("Credentials not accepted: " + e1.getMessage());
				 } catch (IOException e2){
					 logger.error("Key File not found or no permission: " + e2.getMessage());
				 }

				 /*
				  * Try to upload the defined file as a plain text file to the Storage service
				  * 
				  */
				 try {
					 File upload = new File(file);
					 uploadStream(upload.getName(), null, new FileInputStream(upload), bucket);
					 logger.info("File upload accepeted!");
				 } catch (GeneralSecurityException e) {
					 logger.error("File not uploaded: " + e.getMessage());
				 } catch (IOException e2){
					 logger.error("Uploading file not found or no permission: " + e2.getMessage());
				 }


				 actiontarget.setText("Uploaded");
			 }
		 });


		 primaryStage.show();
	}

	/**
	 * Method initiated on start, args are the text placed after the name of the jar file
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		launch(args);

		/*
		 * Add the different options for the parser
		 */
		/*Options options = new Options();
		options.addOption("bucket", true, "name of the bucket to be used");
		options.addOption("keyfile", true, "name of the file to be used as the key");
		options.addOption("file", true, "name of the text file to upload");

		CommandLineParser parser = new DefaultParser();
		CommandLine line;
		String file = "";
		String keyFile = "";

		/*
		 * Attempt to parse the arguments when the program is run from the command line
		 * For example java -jar backupbuddy.jar -keyfile json -bucket mybucket -file mydocument.txt
		 * This initiation would set the variable to their declared value
		 */
		/*try {
			line = parser.parse(options, args);
			BUCKET_NAME = line.getOptionValue("bucket", "datadouble");
			keyFile = line.getOptionValue("keyfile", "json");
			file = line.getOptionValue("file", "testfile.txt");
		} catch (ParseException e2) {
			logger.error("Failed to parse arguments: " + e2);
			System.exit(1);
		}

		/*
		 * Attempt to find the JSON key file and throw an error if not found
		 * If found, authenticate with the Google servers
		 */
		/*try {
			File key = new File(System.getProperty("user.dir") + File.separator + keyFile);
			storageService = retrieveStorageService(key);
			logger.info("Credentials accepted! Storage service retrieved.");
		} catch (GeneralSecurityException e1) {
			logger.error("Credentials not accepted: " + e1.getMessage());
		} catch (IOException e2){
			logger.error("Key File not found or no permission: " + e2.getMessage());
		}

		/*
		 * Try to upload the defined file as a plain text file to the Storage service
		 * 
		 */
		/*try {
			File upload = new File(System.getProperty("user.dir") + File.separator + file);
			uploadStream(file, null, new FileInputStream(upload), BUCKET_NAME);
			logger.info("File upload accepeted!");
		} catch (GeneralSecurityException e) {
			logger.error("File not uploaded: " + e.getMessage());
		} catch (IOException e2){
			logger.error("Uploading file not found or no permission: " + e2.getMessage());
		}*/



	}

	/**
	 * Get all of the objects that are in a bucket
	 * 
	 * @param bucketName name of the bucket you'd like parse data from
	 * @return a list of all of the objects in the bucket
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static List<StorageObject> listBucket(String bucketName) throws IOException, GeneralSecurityException {
		Storage.Objects.List listRequest = storageService.objects().list(bucketName);

		List<StorageObject> rt = new ArrayList<StorageObject>();
		Objects objects; //Google's fancy array

		/*
		 * Use a do-while statement to allow for a more compact method. If it was just
		 * a while statement you would have to execute once before going into the loop so that server knows
		 * what entry is next
		 */
		do {
			objects = listRequest.execute();
			rt.addAll(objects.getItems());

			listRequest.setPageToken(objects.getNextPageToken());
		} while (null != objects.getNextPageToken());

		return rt;
	}

	/**
	 * Authenticate to the Storage Service and Build the Storage service for api requests
	 * 
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static Storage retrieveStorageService(File keyfile) throws IOException, GeneralSecurityException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential cred = GoogleCredential.fromStream(new FileInputStream(keyfile), httpTransport, JSON_FACTORY);

		/*
		 * Allows access to all of the Storage API methods
		 */
		if (cred.createScopedRequired()) {
			cred = cred.createScoped(StorageScopes.all());
		}

		storageService = new Storage.Builder(httpTransport, JSON_FACTORY, cred).setApplicationName(APPLICATION_NAME).build();

		return storageService;
	}

	/**
	 *  Retrieve a bucket by a specific name
	 *  
	 * @param bucketName
	 * @return bucket requested, null if storageService is not active
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
		if(storageService == null) return null;

		Storage.Buckets.Get bucketRequest = storageService.buckets().get(bucketName);
		bucketRequest.setProjection("full");
		return bucketRequest.execute();
	}

	/**
	 * Upload a file to the Google Cloud Storage servers
	 * Takes any type of InputStream, converts to an InputStreamContent and then uploads
	 * 
	 * @param name
	 * @param contentType
	 * @param stream
	 * @param bucketName
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static void uploadStream(String name, String contentType, InputStream stream, String bucketName) throws IOException, GeneralSecurityException {
		InputStreamContent contentStream = new InputStreamContent(contentType, stream);

		/*
		 * Sets the metadata such as name, access permissions, etc.
		 */
		StorageObject objectMetadata = new StorageObject().setName(name)
				.setAcl(Arrays.asList(new ObjectAccessControl().setEntity("allUsers").setRole("READER")));

		Storage.Objects.Insert insertRequest = storageService.objects().insert(bucketName, objectMetadata, contentStream);

		insertRequest.execute();
	}



	private static void configureFileChooser(final FileChooser fileChooser) {      
		fileChooser.setTitle("Select...");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}
}
