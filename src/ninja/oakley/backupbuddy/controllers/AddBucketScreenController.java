package ninja.oakley.backupbuddy.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import ninja.oakley.backupbuddy.BackupBuddy;

public class AddBucketScreenController implements Initializable {
	
	private BackupBuddy instance;
	
	@FXML
	private TextField bucketNameField;
	
	@FXML
	private ChoiceBox<String> typeChoiceBox;
	
	@FXML
	private ChoiceBox<String> regionChoiceBox;
	
	public AddBucketScreenController(BackupBuddy instance){
		this.instance = instance;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> types = FXCollections.observableArrayList();
		ObservableList<String> regions = FXCollections.observableArrayList();
		
		typeChoiceBox.setItems(types);
		regionChoiceBox.setItems(regions);
		
	}
	
	@FXML
	public void onConfirm(){
		
		
		
	}

}
