package ninja.oakley.backupbuddy.project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.configuration.SaveConfigurationRunnable;

public class CreateProjectRunnable implements Runnable {

    private BackupBuddy instance;
    private Path jsonKey;

    public CreateProjectRunnable(BackupBuddy instance, Path jsonKey) {
        this.instance = instance;
        this.jsonKey = jsonKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> userData = mapper.readValue(jsonKey.toFile(), Map.class);
            String projectId = (String) userData.get("project_id");

            ProjectController manager = new ProjectController.Builder(
                    new Project(projectId, jsonKey.toAbsolutePath().toString())).build();

            manager.constructStorageService();

            instance.getProjects().put(projectId, manager);
            instance.getConfigurationManager().addProject(manager.getProject());

            new Thread(new SaveConfigurationRunnable(instance)).start();
            instance.getBaseController().refresh();
        } catch (FileNotFoundException e) {

        } catch (JsonParseException e) {

        } catch (JsonMappingException e) {

        } catch (IOException e) {

        } catch (GeneralSecurityException e) {

        }
    }
}
