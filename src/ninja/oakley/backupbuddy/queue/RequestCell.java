package ninja.oakley.backupbuddy.queue;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class RequestCell extends ListCell<Request> {

    private ProgressBar progressBar = new ProgressBar();
    private Text text = new Text();
    private HBox hBox = new HBox();

    public RequestCell() {
        hBox.getChildren().addAll(text, progressBar);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);
    }

    @Override
    protected void updateItem(Request request, boolean empty) {
        super.updateItem(request, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            progressBar.setProgress(request.getProgress());

            if (progressBar.getProgress() == 1.0) {
                progressBar.getStyleClass().add("green-bar");
            }

            text.setText(request.toString());
            text.setFont(Font.font(12.0));
            setGraphic(hBox);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

}