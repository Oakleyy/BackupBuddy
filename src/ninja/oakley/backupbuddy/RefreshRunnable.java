package ninja.oakley.backupbuddy;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.controllers.BaseScreenController;

public class RefreshRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(RefreshRunnable.class);

    private BackupBuddy instance;

    public RefreshRunnable(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        BaseScreenController base = instance.getBaseController();
        base.updateProjectList();

        try {
            base.updateBucketList();
        } catch (IOException | GeneralSecurityException e) {
            logger.error("Error updating bucket list: " + e);
        }

        if (base.getCurrentBucket() != null && !base.getCurrentBucket().isEmpty()) {
            try {
                base.updateFileList();
            } catch (IOException | GeneralSecurityException e) {
                logger.error("Error updating file list: " + e);
            }
        }
    }

}
