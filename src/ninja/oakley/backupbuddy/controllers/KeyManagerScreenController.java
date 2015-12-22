package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import ninja.oakley.backupbuddy.encryption.KeyHandler;

public class KeyManagerScreenController extends AbstractScreenController<Pane> {

    @FXML
    private TableView<KeyHandler> table;

    @FXML
    private TableColumn<KeyHandler, String> tableName;

    @FXML
    private TableColumn<KeyHandler, String> tableFingerPrint;

    @FXML
    private TableColumn<KeyHandler, String> tableFilePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableName.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("name"));
        tableFingerPrint.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("fingerPrint"));
        tableFilePath.setCellValueFactory(new PropertyValueFactory<KeyHandler, String>("filePath"));
    }

    @FXML
    public void onAddKey() {

    }

    @FXML
    public void onRemoveKey() {

    }

    @Override
    public void load() throws IOException {
        FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "KeyManager.fxml");
        setController(baseLoader, this);
        base = (Pane) baseLoader.load();

    }

}
