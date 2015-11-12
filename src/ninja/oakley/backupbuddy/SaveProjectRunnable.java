package ninja.oakley.backupbuddy;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveProjectRunnable implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(SaveProjectRunnable.class);
	
	private BackupBuddy instance;
	private Project project;
	
	public SaveProjectRunnable(BackupBuddy instance, Project project){
		this.instance = instance;
		this.project = project;
	}
	
	@Override
	public void run() {
		try {
			instance.getConfigurationManager().saveProjectProfile(project);
			logger.info("Saved profile named " + project.getProjectId());
		} catch (ConfigurationException e) {
			logger.error("Error parsing configuration: " + e);
		} catch (IOException e) {
			logger.error("Error saving file: " + e);
		}
	}

}
