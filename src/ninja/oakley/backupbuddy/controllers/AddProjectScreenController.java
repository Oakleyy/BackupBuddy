package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.SaveProjectRunnable;
import ninja.oakley.backupbuddy.project.Project;
import ninja.oakley.backupbuddy.project.ProjectController;

public class AddProjectScreenController extends AbstractScreenController<Pane> {

    private static final Logger logger = LogManager.getLogger(AddProjectScreenController.class);
    private BackupBuddy instance;

    private FileChooser fileChooser;
    private Scene scene;

    @FXML
    private TextField projectIdField;

    @FXML
    private TextField jsonKeyField;

    public AddProjectScreenController(BackupBuddy instance) throws IOException {
        this.instance = instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    @FXML
    public void onTest() {

    }

    @FXML
    public void onConfirm() throws GeneralSecurityException {
        String jsonKey = jsonKeyField.getText();
        String projectId = projectIdField.getText();

        if (jsonKey.isEmpty()) {
            logger.error("Key is empty");
            return;
        }

        if (projectId.isEmpty()) {
            logger.error("Project Id is empty");
            return;
        }

        if (instance.getProjects().containsKey(projectId.toLowerCase())) {
            logger.error("Application already used.");
            return;
        }

        ProjectController manager;
        try {
            manager = new ProjectController.Builder(new Project(projectId, jsonKey)).build();
        } catch (FileNotFoundException e) {
            logger.error("Key not found.");
            return;
        }

        instance.getProjects().put(projectId, manager);
        logger.info("Success creating " + manager.getProjectId());

        closeWindow();

        new Thread(new SaveProjectRunnable(instance, manager.getProject())).start();
        instance.getBaseController().refresh();
    }

    @FXML
    public void onOpenFileChooser() {
        File file = fileChooser.showOpenDialog(instance.getSecondaryStage());
        if (file != null) {
            jsonKeyField.setText(file.getAbsolutePath());
        }
    }

    public void openWindow() {
        if (scene == null) {
            scene = new Scene(getBase());
        }

        Stage stage = instance.getSecondaryStage();
        stage.setScene(scene);
        stage.show();
    }

    public void closeWindow() {
        Stage stage = instance.getSecondaryStage();
        stage.setScene(null);
        stage.hide();

        projectIdField.clear();
        jsonKeyField.clear();
    }

    @Override
    public void load() throws IOException {
        FXMLLoader addProjectLoader = loadFxmlFile(AddProjectScreenController.class, "AddProject.fxml");
        setController(addProjectLoader, this);
        base = (Pane) addProjectLoader.load();
    }

}
