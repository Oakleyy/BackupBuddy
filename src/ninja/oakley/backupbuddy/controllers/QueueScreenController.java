package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.queue.QueueListViewSkin;
import ninja.oakley.backupbuddy.queue.Request;
import ninja.oakley.backupbuddy.queue.RequestCell;

public class QueueScreenController extends AbstractScreenController<Pane> {

    private BackupBuddy instance;

    private Scene scene;

    @FXML
    private ListView<Request> queueList;

    public QueueScreenController(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        queueList.setSkin(new QueueListViewSkin<Request>(queueList));
        queueList.getStylesheets().add(RequestCell.class.getResource("progressbar.css").toExternalForm());
        queueList.setCellFactory(new Callback<ListView<Request>, ListCell<Request>>() {
            @Override
            public ListCell<Request> call(ListView<Request> list) {
                return new RequestCell();
            }
        });
    }

    public void addItem(Request r) {
        ObservableList<Request> list = FXCollections.observableArrayList(queueList.getItems());
        list.add(r);
        queueList.setItems(list);
    }

    public void openWindow() {
        if (scene == null) {
            scene = new Scene(getBase());
        }

        Stage stage = instance.getQueueStage();
        stage.setScene(scene);
        stage.setY(instance.getPrimaryStage().getY());
        stage.setX(instance.getPrimaryStage().getX() + instance.getPrimaryStage().getWidth() + 1);
        stage.show();
    }

    public void closeWindow() {
        Stage stage = instance.getQueueStage();
        stage.hide();

    }

    @SuppressWarnings("unchecked")
    public void refresh() {
        ((QueueListViewSkin<Request>) queueList.getSkin()).refresh();
    }

    @Override
    public void load() throws IOException {
        FXMLLoader queueLoader = loadFxmlFile(QueueScreenController.class, "Queue.fxml");
        setController(queueLoader, this);
        base = (Pane) queueLoader.load();
    }
}
