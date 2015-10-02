package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
	private static final String APPLICATION_NAME = "Backup Buddy/1.1";

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

		Scene scene = new Scene(grid, 600, 275);
		final FileChooser fileChooser = new FileChooser();
		primaryStage.setScene(scene);

		Text title = new Text("Backup Buddy!");
		title.setFont(Font.font("Courier New", FontWeight.NORMAL, 20));
		grid.add(title, 0, 0);

		/*
		 * File
		 */
		Label filePathLabel = new Label("File:     ");
		TextField fileField = new TextField();
		Button fileSelButton = new Button("...");
		HBox fileBox = new HBox(10);
		
		fileField.setPrefWidth(225);
		fileBox.getChildren().addAll(filePathLabel, fileField, fileSelButton);
		grid.add(fileBox, 0, 1, 4, 1);
		
		fileSelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(fileChooser);
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					fileField.setText(file.getAbsolutePath());
				}
			}
		});

		/*
		 * Bucket
		 */
		Label bucketNameLabel = new Label("Bucket:");
		TextField bucketField = new TextField();
		HBox bucketBox = new HBox(10);
		
		bucketField.setPrefWidth(225 - fileSelButton.getWidth());
		bucketBox.getChildren().addAll(bucketNameLabel, bucketField);
		grid.add(bucketBox, 0, 2, 4, 1);

		/*
		 * Keyfile
		 */
		Label keyFilePathLabel = new Label("Keyfile:");
		TextField keyFileField = new TextField();
		Button keyFileSelButton = new Button("...");	
		HBox keyFileBox = new HBox(10);
		
		keyFileField.setPrefWidth(225);
		keyFileBox.getChildren().addAll(keyFilePathLabel, keyFileField, keyFileSelButton);
		grid.add(keyFileBox, 0, 3, 4, 1);

		keyFileSelButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(fileChooser);
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					keyFileField.setText(file.getAbsolutePath());
				}
			}
		});
		
		
		/*
		 * Error Box
		 */
		final Text actiontarget = new Text();
		grid.add(actiontarget, 1, 6);

		Button uploadBtn = new Button("Upload");
		HBox hbBtn = new HBox(10);
		
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(uploadBtn);
		grid.add(hbBtn, 1, 4);

		uploadBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String file = fileField.getText();
				String keyFile = keyFileField.getText();
				String bucket = bucketField.getText();


				if(keyFile.isEmpty()){
					keyFilePathLabel.setTextFill(Color.RED);
					event.consume();
				}

				if(file.isEmpty()){
					filePathLabel.setTextFill(Color.RED);
					event.consume();
				}

				if(bucket.isEmpty()){
					bucketNameLabel.setTextFill(Color.RED);
					event.consume();
				}
				
				if(event.isConsumed()){
					sendActionMessage(actiontarget, "One or more of the fields are blank.", Color.RED);
					return;
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
					actiontarget.setText("Credentials not accepted.");
					actiontarget.setFill(Color.RED);
					event.consume();
				} catch (IOException e2){
					logger.error("Key File not found or no permission: " + e2.getMessage());
					actiontarget.setText("Key file not found");
					actiontarget.setFill(Color.RED);
					event.consume();
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
					actiontarget.setText("File not uploaded.");
					actiontarget.setFill(Color.RED);
					event.consume();
				} catch (IOException e2){
					logger.error("Uploading file not found or no permission: " + e2.getMessage());
					actiontarget.setText("File not found");
					actiontarget.setFill(Color.RED);
					event.consume();
				}


				actiontarget.setText("Uploaded");
				actiontarget.setFill(Color.GREEN);
			}
		});

		ListView<String> list = new ListView<String>();
		ObservableList<String> items = FXCollections.observableArrayList();
		list.setItems(items);
		list.setPrefSize(200, 200);

		grid.add(list, 5, 1, 1, 3);

		Button refreshBtn = new Button("Refresh");
		HBox hRefreshBtn = new HBox(10);
		
		hRefreshBtn.setAlignment(Pos.CENTER);
		hRefreshBtn.getChildren().add(refreshBtn);
		grid.add(hRefreshBtn, 5, 4);

		refreshBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String keyFile = keyFileField.getText();
				String bucket = bucketField.getText();


				if(keyFile.isEmpty()){
					actiontarget.setText("One or more of the fields are blank.");
					keyFilePathLabel.setTextFill(Color.RED);
					event.consume();
				}

				if(bucket.isEmpty()){
					actiontarget.setText("One or more of the fields are blank.");
					bucketNameLabel.setTextFill(Color.RED);
					event.consume();
				}

				try {
					File key = new File(keyFile);
					storageService = retrieveStorageService(key);
					logger.info("Credentials accepted! Storage service retrieved.");
				} catch (GeneralSecurityException e1) {
					logger.error("Credentials not accepted: " + e1.getMessage());
					actiontarget.setText("Credentials not accepted.");
					actiontarget.setFill(Color.RED);
					event.consume();
				} catch (IOException e2){
					logger.error("Key File not found or no permission: " + e2.getMessage());
					actiontarget.setText("Key file not found");
					actiontarget.setFill(Color.RED);
					event.consume();
				}

				List<String> files = new ArrayList<String>();
				try {
					List<StorageObject> objects = listBucket(bucket);
					Iterator<StorageObject> iterObj = objects.iterator();

					while(iterObj.hasNext()){
						StorageObject next = iterObj.next();
						files.add(next.getName());
					}


				} catch (IOException e) {


				} catch (GeneralSecurityException e) {


				}

				if(files.size() > 0){
					items.clear();
					items.addAll(files);
					list.setItems(items);
					
					actiontarget.setText("List Updated");
					actiontarget.setFill(Color.GREEN);
				} else {

				}

			}
		});

		primaryStage.show();
	}

	/**
	 * Method initiated on start
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		launch(args);

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
	
	public static void sendActionMessage(Text target, String st, Color color){
		target.setText(st);
		target.setFill(color);
	}
}
