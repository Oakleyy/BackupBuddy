package ninja.oakley.backupbuddy.configuration;

import java.util.ArrayList;
import java.util.List;

import ninja.oakley.backupbuddy.project.Project;

public final class Configuration {

    private List<Project> projects;

    public Configuration() {
        projects = new ArrayList<>();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
