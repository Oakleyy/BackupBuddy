package ninja.oakley.backupbuddy.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.queue.Request;
import ninja.oakley.backupbuddy.queue.RequestCell;

public class QueueScreenController implements Initializable {

    private BackupBuddy instance;
    
    private Scene scene;
    
    @FXML
    private ListView<Request> queueList;

    public QueueScreenController(BackupBuddy instance){
        this.instance = instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        queueList.setCellFactory(new Callback<ListView<Request>, ListCell<Request>>() {
            @Override public ListCell<Request> call(ListView<Request> list) {
                return new RequestCell();
            }
        });
    }
    
    public void openWindow() {
        if (scene == null) {
            scene = new Scene(instance.queuePane);
        }

        Stage stage = instance.getQueueStage();
        stage.setScene(scene);
        stage.show();
    }

    public void closeWindow() {
        Stage stage = instance.getQueueStage();
        stage.hide();

    }

}
