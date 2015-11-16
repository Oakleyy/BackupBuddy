package ninja.oakley.backupbuddy.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Project {

    private String projectId;
    private Path filePath;

    public Project(String projectId, String path) {
        this.projectId = projectId;
        filePath = Paths.get(path);
    }

    public Project() {

    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(filePath.toFile());
    }

}
