package ninja.oakley.backupbuddy.configuration;

import java.util.ArrayList;
import java.util.List;

import ninja.oakley.backupbuddy.encryption.Key;
import ninja.oakley.backupbuddy.project.Project;

public final class Configuration {

    private List<Project> projects;
    private List<Key> keys;

    public Configuration() {
        projects = new ArrayList<>();
        keys = new ArrayList<>();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }
}
