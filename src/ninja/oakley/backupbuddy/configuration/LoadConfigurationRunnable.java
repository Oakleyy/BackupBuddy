package ninja.oakley.backupbuddy.configuration;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.BackupBuddy;

public class LoadConfigurationRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(LoadConfigurationRunnable.class);

    private BackupBuddy instance;

    public LoadConfigurationRunnable(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        try {
            instance.getConfigurationManager().loadProjectProfiles();
            logger.info("Finished loading Projects.");
        } catch (ConfigurationException e) {
            logger.error("Error reading the configuration file. " + e);
        } catch (IOException e) {
            logger.error("Could not find the configuration file. " + e);
        }
    }

}
