package ninja.oakley.backupbuddy.project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.RefreshRunnable;

public class LoadProjectsRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(LoadProjectsRunnable.class);

    private BackupBuddy instance;
    private List<Project> projects;

    public LoadProjectsRunnable(BackupBuddy instance, List<Project> projects) {
        this.instance = instance;
        this.projects = projects;
    }

    @Override
    public void run() {
        HashMap<String, ProjectController> rt = new HashMap<>();

        Iterator<Project> iter = projects.iterator();
        while (iter.hasNext()) {
            Project next = iter.next();

            try {
                ProjectController controller = new ProjectController.Builder(next).build();
                controller.constructStorageService();
                rt.put(controller.getProjectId(), controller);
            } catch (FileNotFoundException e) {
                logger.error("Key File not found when loading project '" + next.getProjectId() + "'");
            } catch (IOException e) {
                logger.error("Error loading key file with '" + next.getProjectId() + "'");
            } catch (GeneralSecurityException e) {
                logger.error("Key file not valid: '" + next.getProjectId() + "'");
            }
        }

        instance.getProjects().putAll(rt);
        Platform.runLater(new RefreshRunnable(instance, null, null, true));
    }

}
