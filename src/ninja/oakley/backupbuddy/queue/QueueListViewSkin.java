package ninja.oakley.backupbuddy.queue;

import com.sun.javafx.scene.control.skin.ListViewSkin;

import javafx.scene.control.ListView;

public class QueueListViewSkin<T> extends ListViewSkin<T> {

    public QueueListViewSkin(ListView<T> listView) {
        super(listView);
    }

    public void refresh() {
        super.flow.recreateCells();
    }

}
