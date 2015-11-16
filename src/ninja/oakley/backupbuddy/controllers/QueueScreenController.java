package ninja.oakley.backupbuddy.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import ninja.oakley.backupbuddy.queue.Request;

public class QueueScreenController {

    @FXML
    private ListView<ListCell<Request>> queueList;

    public void updateCell() {
        ListCell<Request> cell = queueList.getItems().get(0);
    }

}
