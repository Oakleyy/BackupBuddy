package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.BucketManager;

public class AddProjectScreenController implements Initializable {

	private static final Logger logger = LogManager.getLogger(AddProjectScreenController.class);
	private BackupBuddy instance;

	private FileChooser fileChooser;
	private Scene scene;

	@FXML
	private TextField jsonKeyField;

	@FXML
	private TextField projectIdField;

	public AddProjectScreenController(BackupBuddy instance){
		this.instance = instance;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fileChooser = new FileChooser();
		fileChooser.setTitle("Select...");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}

	@FXML
	public void onConfirm(){

		String jsonKeyPath = jsonKeyField.getText();
		if(jsonKeyPath.isEmpty()){
			logger.error("JSON key path is empty.");
			return;
		}

		String projectId = projectIdField.getText().trim();
		if(projectId.isEmpty()){
			logger.error("Project ID is empty.");
			return;
		}

		if(instance.getProjects().containsKey(projectId.toLowerCase())){
			logger.error("Application Name already taken.");
			return;
		}

		Path jsonKey = Paths.get(jsonKeyPath);
		BucketManager manager;
		try {
			manager = new BucketManager.Builder()
					.setProjectId(projectId)
					.setCredentialInputStream(new FileInputStream(jsonKey.toFile()))
					.build();

			manager.constructStorageService();
			instance.getProjects().put(projectId, manager);
			logger.info("Success authenticating " + manager.getProjectId());
		} catch (IOException e) {
			logger.error("Not a valid file at: " + jsonKeyPath);
			return;
		} catch (GeneralSecurityException e) {
			logger.error("Key not accepted.");
			return;
		}
		
		closeWindow();

		instance.getBaseController().updateProjectList();

		instance.getBaseController().setCurrentBucketManager(manager);

		try {
			instance.getBaseController().updateBucketList();
		} catch (IOException e) {
			logger.error("Trouble authenticating while retrieving buckets: " + e);
		} catch (GeneralSecurityException e) {
			logger.error("Cannot retrieve list of Buckets: " + e);
		}

	}

	@FXML
	public void onOpenFileChooser(){
		File file = fileChooser.showOpenDialog(instance.getSecondaryStage());
		if (file != null) {
			jsonKeyField.setText(file.getAbsolutePath());
		}
	}

	public void openWindow(){
		if (scene == null) scene = new Scene(instance.addProjectPane);

		Stage stage = instance.getSecondaryStage();
		stage.setScene(scene);
		stage.show();
	}

	public void closeWindow(){
		Stage stage = instance.getSecondaryStage();
		stage.setScene(null);
		stage.hide();
	}

}
