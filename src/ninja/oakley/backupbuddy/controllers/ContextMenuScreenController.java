package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import ninja.oakley.backupbuddy.BackupBuddy;

public class ContextMenuScreenController extends AbstractScreenController<ContextMenu> {

    private BackupBuddy instance;

    public ContextMenuScreenController(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void load() throws IOException {
        FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "ContextMenu.fxml");
        setController(baseLoader, this);
        base = (ContextMenu) baseLoader.load();
    }

    @FXML
    public void onDownload() {
        instance.getBaseController().onDownloadSelect();
    }

    @FXML
    public void onDelete() {

    }

    @FXML
    public void onGetInfo() {

    }

}
