package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitMenuButton;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.BucketManager;

public class BaseScreenController implements Initializable {

	private static final Logger logger = LogManager.getLogger(BaseScreenController.class);
	private BackupBuddy instance;

	private String prevProject;
	private String prevBucket;

	@FXML
	private ComboBox<String> projectComboBox;

	@FXML
	private ComboBox<String> bucketComboBox;

	@FXML
	private ListView<String> fileList;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private SplitMenuButton actionMenu;

	public BaseScreenController(BackupBuddy instance){
		this.instance = instance;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	@FXML
	public void onSettingsClick(){

	}

	@FXML
	public void onProjectSelect(){
		String current = projectComboBox.getValue();
		
		if(current == null || current.isEmpty() || current.equalsIgnoreCase(prevProject)){
			return;
		}

		BucketManager bucketManager = instance.getProjects().get(current);
		if(bucketManager == null){
			logger.error("No BucketManager exists for " + current);
			return;
		}
		
		setCurrentBucketManager(bucketManager);
		
		try {
			updateBucketList();
		} catch (IOException e) {
			logger.error("Couldn't load authentication " + e);
			return;
		} catch (GeneralSecurityException e) {
			logger.error("Couldn't authenticate " + e);
			return;
		}
		
		prevProject = current;

	}

	@FXML
	public void onAddProject(){
		if(instance.getSecondaryStage().isShowing()) {
			logger.warn("Secondary window already being used.");
			return;
		}

		instance.getAddProjectController().openWindow();
	}

	@FXML
	public void onBucketSelect(){
		String currentValue = bucketComboBox.getValue();
		
		if(currentValue == null || currentValue.isEmpty() || currentValue.equalsIgnoreCase(prevBucket)){
			return;
		}
		
		try {
			updateFileList();
		} catch (IOException e) {
			logger.error("Couldn't load authentication " + e);
			return;
		} catch (GeneralSecurityException e) {
			logger.error("Couldn't authenticate " + e);
			return;
		}
		
		prevBucket = currentValue;
	}

	@FXML
	public void onAddBucket(){
		if(instance.getSecondaryStage().isShowing()) {
			logger.warn("Secondary window already being used.");
			return;
		}
		
		
	}

	@FXML
	public void onActionSelect(){

	}

	public void setCurrentBucketManager(BucketManager bucketManager){
		if(projectComboBox.getItems().contains(bucketManager.getProjectId()) 
				|| instance.getProjects().containsKey(bucketManager.getProjectId())){
			projectComboBox.setValue(bucketManager.getProjectId());
			return;
		}
		
	}

	public BucketManager getCurrentBucketManager(){
		return instance.getProjects().get(projectComboBox.getValue());
	}
	

	public void updateProjectList(){
		ObservableList<String> items = FXCollections.observableArrayList(instance.getProjects().keySet());
		projectComboBox.setItems(items);
	}

	public void updateBucketList() throws IOException, GeneralSecurityException {
		ObservableList<String> items = FXCollections.observableArrayList();
		List<Bucket> buckets = getCurrentBucketManager().getBuckets();
		ListIterator<Bucket> iter = buckets.listIterator();

		while(iter.hasNext()){
			Bucket bucket = iter.next();
			items.add(bucket.getName());
		}

		bucketComboBox.setItems(items);
	}
	
	public void updateFileList() throws IOException, GeneralSecurityException {
		String currentBucket = bucketComboBox.getValue();
		List<StorageObject> files = getCurrentBucketManager().listBucket(currentBucket);
		
		ObservableList<String> items = FXCollections.observableArrayList();
		ListIterator<StorageObject> iter = files.listIterator();
		while(iter.hasNext()){
			StorageObject file = iter.next();
			items.add(file.getName());
		}
		
		fileList.setItems(items);
	}
	
	public void clearFileList(){
		ObservableList<String> items = FXCollections.observableArrayList();
		fileList.setItems(items);
	}

}
