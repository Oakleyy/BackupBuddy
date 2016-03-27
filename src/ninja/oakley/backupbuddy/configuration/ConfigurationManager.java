package ninja.oakley.backupbuddy.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import ninja.oakley.backupbuddy.encryption.Key;
import ninja.oakley.backupbuddy.project.Project;

public class ConfigurationManager {

    private Configuration config;

    public ConfigurationManager() {
    }

    public void saveConfig() throws IOException {
        Yaml yaml = new Yaml();
        String dump = yaml.dump(getConfig());

        Path path = Paths.get("backupbuddy.yml");
        Files.write(path, Arrays.asList(dump), Charset.forName("UTF-8"));
    }

    public void loadConfig() throws FileNotFoundException {
        Constructor cont = new Constructor(Configuration.class);

        TypeDescription configDesc = new TypeDescription(Configuration.class);
        configDesc.putListPropertyType("projects", Project.class);
        configDesc.putListPropertyType("keys", Key.class);
        cont.addTypeDescription(configDesc);

        Yaml yaml = new Yaml(cont);
        config = (Configuration) yaml.load(new FileInputStream(Paths.get("backupbuddy.yml").toFile()));
    }

    public void createConfig() throws IOException {
        Path path = Paths.get("backupbuddy.yml");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        config = new Configuration();
    }

    public boolean configExists() {
        return Paths.get("backupbuddy.yml").toFile().exists();
    }

    public boolean isConfigBlank() {
        return Paths.get("backupbuddy.yml").toFile().length() == 0L;
    }

    public void addProject(Project project) {
        getConfig().getProjects().add(project);
    }

    public void removeProject(Project project) {
        getConfig().getProjects().remove(project);
    }

    public List<Project> getProjects() {
        return getConfig().getProjects();
    }

    public void addKey(Key key) {
        getConfig().getKeys().add(key);
    }

    public void removeKey(Key key) {
        getConfig().getKeys().remove(key);
    }

    public List<Key> getKeys() {
        return getConfig().getKeys();
    }

    private Configuration getConfig() {
        return config;
    }

}
