package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.configuration.SaveConfigurationRunnable;
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

    @FXML
    private ComboBox<KeyHandler> currentKey;

    public KeyManagerScreenController(BackupBuddy instance) {
        this.instance = instance;
        fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableName.setCellValueFactory(new Callback<CellDataFeatures<KeyHandler, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<KeyHandler, String> data) {
                return new ReadOnlyObjectWrapper<String>(data.getValue().getKey().getName());
            }
        });

        tableFingerPrint.setCellValueFactory(new Callback<CellDataFeatures<KeyHandler, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<KeyHandler, String> data) {
                return new ReadOnlyObjectWrapper<String>(data.getValue().getKey().getFingerPrint());
            }
        });

        tableFilePath.setCellValueFactory(new Callback<CellDataFeatures<KeyHandler, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<KeyHandler, String> data) {
                return new ReadOnlyObjectWrapper<String>(data.getValue().getKey().getKeyPath());
            }
        });
    }

    @FXML
    public void onAddKey() {
        File file = fileChooser.showOpenDialog(instance.getSecondaryStage());

        try {
            instance.getEncryptionManager().addKey(null, file.toPath());
            new Thread(new SaveConfigurationRunnable(instance)).start();

            update();
        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        } catch (IOException e) {

        } catch (KeyAlreadyExistsException e) {

        }

    }

    @FXML
    public void onRemoveKey() {
        KeyHandler handler = table.getSelectionModel().getSelectedItem();

        if (handler == null) {
            return;
        }
        instance.getEncryptionManager();
        update();
    }
    
    public KeyHandler getCurrentKeyHandler(){
        return currentKey.getValue();
    }

    public void openWindow() {
        if (scene == null) {
            scene = new Scene(getBase());
        }

        update();

        Stage stage = instance.getSecondaryStage();
        stage.setScene(scene);
        stage.show();
    }

    public void update() {
        ObservableList<KeyHandler> items = FXCollections.observableArrayList(instance.getEncryptionManager().getKeyHandlers());
        table.setItems(items);
        currentKey.setItems(items);
    }

    @Override
    public void load() throws IOException {
        FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "KeyManager.fxml");
        setController(baseLoader, this);
        base = (Pane) baseLoader.load();
    }

}
