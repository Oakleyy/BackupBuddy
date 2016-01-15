package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import com.google.api.services.storage.model.Bucket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.encryption.KeyAlreadyExistsException;
import ninja.oakley.backupbuddy.encryption.KeyHandler;

public class KeyManagerScreenController extends AbstractScreenController<Pane> {

    private BackupBuddy instance;
    
    private Scene scene;
    private FileChooser fileChooser;
    
    @FXML
    private TableView<KeyHandler> table;

    @FXML
    private TableColumn<KeyHandler, String> tableName;

    @FXML
    private TableColumn<KeyHandler, String> tableFingerPrint;

    @FXML
    private TableColumn<KeyHandler, String> tableFilePath;
    
    public KeyManagerScreenController(BackupBuddy instance){
        this.instance = instance;
        fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableName.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("name"));
        tableFingerPrint.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("fingerPrint"));
        tableFilePath.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("filePath"));
    }

    @FXML
    public void onAddKey() {
        File file = fileChooser.showOpenDialog(instance.getSecondaryStage());
        
        try {
            instance.getEncryptionManager().addKey(null, file.toPath());
            
            updateTable();
            
        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        } catch (IOException e) {

        } catch (KeyAlreadyExistsException e) {

        }
        
    }

    @FXML
    public void onRemoveKey() {

    }
    
    public void openWindow() {
        if (scene == null) {
            scene = new Scene(getBase());
        }

        Stage stage = instance.getSecondaryStage();
        stage.setScene(scene);
        stage.show();
    }
    
    public void updateTable() {
        ObservableList<KeyHandler> items = FXCollections.observableArrayList(instance.getEncryptionManager().getKeyHandlers());
        table.setItems(items);
    }

    @Override
    public void load() throws IOException {
        FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "KeyManager.fxml");
        setController(baseLoader, this);
        base = (Pane) baseLoader.load();

    }

}
