package ninja.oakley.backupbuddy.configuration;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.BackupBuddy;

public class SaveProjectRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(SaveProjectRunnable.class);

    private BackupBuddy instance;

    public SaveProjectRunnable(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        try {
            instance.getConfigurationManager().saveConfig();
            logger.info("Saved config");
        } catch (IOException e) {
            logger.error("Error saving file: " + e);
        }
    }

}
