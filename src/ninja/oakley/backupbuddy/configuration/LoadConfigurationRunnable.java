package ninja.oakley.backupbuddy.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.encryption.Key;
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
            new Thread(new LoadProjectsRunnable(instance, cm.getProjects())).start();
        }
        
        if (cm.getKeys().size() > 0) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Iterator<Key> iter = cm.getKeys().iterator();
                    while (iter.hasNext()) {
                        try {
                            instance.getEncryptionManager().loadKey(iter.next());
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();
            
        }
    }

}
