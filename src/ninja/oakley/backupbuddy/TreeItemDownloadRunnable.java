package ninja.oakley.backupbuddy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;
import ninja.oakley.backupbuddy.queue.DownloadRequest;

public class TreeItemDownloadRunnable implements Runnable {

    private BlockingQueue<TreeItem<String>> queue;
    private BackupBuddy instance;
    private TreeView<String> fileList;
    private Path saveLocation;

    public TreeItemDownloadRunnable(BackupBuddy instance, List<TreeItem<String>> initial, Path saveLocation,
            TreeView<String> fileList) {
        this.instance = instance;
        queue = new LinkedBlockingQueue<TreeItem<String>>(initial);
        this.fileList = fileList;
        this.saveLocation = saveLocation;
    }

    @Override
    public void run() {
        BaseScreenController base = instance.getBaseController();

        while (!queue.isEmpty()) {
            try {
                TreeItem<String> next = queue.take();
                TreeItem<String> parent = next.getParent();
                String path = "";

                while (parent != null && parent != fileList.getRoot()) {
                    path = parent.getValue() + path;
                    parent = parent.getParent();
                }

                path += next.getValue();

                if (!next.isLeaf()) {
                    ListIterator<TreeItem<String>> i = next.getChildren().listIterator();
                    while (i.hasNext()) {
                        TreeItem<String> n = i.next();
                        n.setExpanded(true);
                        fileList.getSelectionModel().select(n);
                        queue.add(n);
                    }
                }

                DownloadRequest req = new DownloadRequest(base.getSelectedProjectController(), path,
                        Paths.get(saveLocation.toString(), path), base.getCurrentBucket());
                instance.getRequestHandler().addRequest(req, !path.endsWith("/"));

            } catch (InterruptedException e) {

            }
        }

    }

}
