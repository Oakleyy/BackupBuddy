package ninja.oakley.backupbuddy;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;

public class RefreshRunnable implements Runnable {

    private BackupBuddy instance;

    private TreeItem<String> files;
    private ObservableList<String> buckets;
    private boolean updateProjects;

    public RefreshRunnable(BackupBuddy instance, TreeItem<String> files, ObservableList<String> buckets,
            boolean updateProjects) {
        this.instance = instance;
        this.files = files;
        this.buckets = buckets;
        this.updateProjects = updateProjects;
    }

    @Override
    public void run() {
        BaseScreenController cont = instance.getBaseController();

        if (files != null) {
            cont.setFileList(files);
        }

        if (buckets != null) {
            cont.setBucketList(buckets);
        }

        if (updateProjects) {
            cont.updateProjectList();
        }

    }

}
