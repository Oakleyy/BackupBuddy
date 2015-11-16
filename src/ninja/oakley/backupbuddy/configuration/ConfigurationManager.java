package ninja.oakley.backupbuddy.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.project.BucketManager;
import ninja.oakley.backupbuddy.project.Project;

public class ConfigurationManager {

    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);

    private BackupBuddy instance;

    private XMLConfiguration config;

    public ConfigurationManager(BackupBuddy instance) {
        this.instance = instance;
    }

    public void saveProjectProfile(Project project) throws ConfigurationException, IOException {
        getConfig().setProperty("accounts/account[projectId = " + project.getProjectId() + "]/path",
                project.getFilePath().toString());
        getConfig().setProperty("accounts/account[projectId = " + project.getProjectId() + "]/projectId",
                project.getProjectId());
        getConfig().save();
        logger.info("Saved: " + project.getProjectId());
    }

    public void loadProjectProfiles() throws ConfigurationException, IOException {
        Iterator<HierarchicalConfiguration> iter = getConfig().configurationsAt("accounts.account").iterator();
        while (iter.hasNext()) {
            HierarchicalConfiguration next = iter.next();
            String projectId = next.getString("projectId");
            String jsonKeyPath = next.getString("path");

            Project project = new Project(projectId, jsonKeyPath);
            BucketManager manager = new BucketManager.Builder(project).build();

            instance.getProjects().put(manager.getProjectId(), manager);
        }
        instance.getBaseController().updateProjectList();
    }

    private XMLConfiguration getConfig() throws IOException, ConfigurationException {
        if (config == null) {
            Path dir = Paths.get("backupbuddy.xml");
            if (!dir.toFile().exists()) {
                Files.touch(dir.toFile());
            }
            config = new XMLConfiguration("backupbuddy.xml");
        }
        return config;
    }

}
