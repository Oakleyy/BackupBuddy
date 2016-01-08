package ninja.oakley.backupbuddy.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.SaveProjectRunnable;

public class CreateProjectRunnable implements Runnable {

    private BackupBuddy instance;
    private Path jsonKey;

    public CreateProjectRunnable(BackupBuddy instance, Path jsonKey){
        this.instance = instance;
        this.jsonKey = jsonKey;
    }

    @Override
    public void run(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> userData = mapper.readValue(jsonKey.toFile(), Map.class);

            String projectId = (String) userData.get("project_id");

            ProjectController manager = new ProjectController.Builder(new Project(projectId, jsonKey.toAbsolutePath().toString())).build();

            instance.getProjects().put(projectId, manager);

            new Thread(new SaveProjectRunnable(instance, manager.getProject())).start();
            instance.getBaseController().refresh();
        } catch (FileNotFoundException e) {
         e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
