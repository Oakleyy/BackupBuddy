package ninja.oakley.backupbuddy.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;

public class Project {

    private String projectId;
    private String filePath;

    public Project(String projectId, String filePath) {
        this.projectId = projectId;
        this.filePath = filePath;
    }

    public Project() {

    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(Paths.get(filePath).toFile());
    }

}
