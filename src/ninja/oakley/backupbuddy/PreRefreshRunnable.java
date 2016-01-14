package ninja.oakley.backupbuddy;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import ninja.oakley.backupbuddy.controllers.BaseScreenController;

public class PreRefreshRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(PreRefreshRunnable.class);

    private BackupBuddy instance;

    public PreRefreshRunnable(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        BaseScreenController base = instance.getBaseController();
        TreeItem<String> files = null;
        ObservableList<String> buckets = null;

        if (base.getSelectedProjectController() != null) {
            try {
                buckets = base.updateBucketList();
            } catch (IOException | GeneralSecurityException e) {
                logger.error("Error updating bucket list: " + e);
            }
        }

        if (base.getCurrentBucket() != null && !base.getCurrentBucket().isEmpty()) {
            try {
                files = base.updateFileList();
            } catch (IOException | GeneralSecurityException e) {
                logger.error("Error updating file list: " + e);
            }
        }

        Platform.runLater(new RefreshRunnable(instance, files, buckets, true));

    }

}
