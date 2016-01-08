package ninja.oakley.backupbuddy.configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.util.IOUtils;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.project.Project;
import ninja.oakley.backupbuddy.project.ProjectController;

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
            ProjectController manager = new ProjectController.Builder(project).build();

            instance.getProjects().put(manager.getProjectId(), manager);
        }
        instance.getBaseController().updateProjectList();
    }
    
    public void copyNewConfigurationFile() throws IOException{
        InputStream is = this.getClass().getResourceAsStream("/default_config.xml");   
        FileOutputStream out = new FileOutputStream("backupbuddy.xml");
                
        IOUtils.copy(is, out, true);
        out.close();
    }
    
    public boolean configExists(){
        return Paths.get("backupbuddy.xml").toFile().exists();
    }

    private XMLConfiguration getConfig() throws IOException, ConfigurationException {
        if (config == null) {
            Path dir = Paths.get("backupbuddy.xml");
            if (!dir.toFile().exists()) {
                copyNewConfigurationFile();
            }
            config = new XMLConfiguration("backupbuddy.xml");
        }
        return config;
    }

}
