package ninja.oakley.backupbuddy.encryption;

public class Key {

    private String name;
    private String keyPath;
    private String fingerPrint;

    public Key(String name, String keyPath) {
        this.name = name;
        this.keyPath = keyPath;
    }
    
    public Key(String name, String keyPath, String fingerPrint) {
        this.name = name;
        this.keyPath = keyPath;
        this.fingerPrint = fingerPrint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }
}
