package ninja.oakley.backupbuddy.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.project.LoadProjectsRunnable;

public class LoadConfigurationRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(LoadConfigurationRunnable.class);

    private BackupBuddy instance;

    public LoadConfigurationRunnable(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        ConfigurationManager cm = instance.getConfigurationManager();

        if (!cm.configExists() || cm.isConfigBlank()) {
            try {
                cm.createConfig();
            } catch (IOException e) {
                logger.error("Error creating config file.");
            }
            return;
        }

        try {
            cm.loadConfig();
        } catch (FileNotFoundException e) {
            logger.error("Config file not found.");
            return;
        }

        if (cm.getProjects().size() > 0) {
            logger.info(cm.getProjects().size());
            new Thread(new LoadProjectsRunnable(instance, cm.getProjects())).start();
        }
    }

}
