package ninja.oakley.backupbuddy.encryption;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ninja.oakley.backupbuddy.BackupBuddy;

public class EncryptionManager {

    private BackupBuddy instance;

    private ConcurrentHashMap<String, KeyHandler> keys = new ConcurrentHashMap<>();

    public EncryptionManager(BackupBuddy instance) {
        this.instance = instance;
    }

    public List<KeyHandler> getKeyHandlers() {
        return new ArrayList<KeyHandler>(keys.values());
    }

    public KeyHandler getKeyHandler(String fingerPrint) {
        return keys.get(fingerPrint);
    }

    public void addKey(String name, Path path)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyAlreadyExistsException {
        Key key = new Key(name, path.toAbsolutePath().toString());
        KeyHandler handler = new KeyHandler(key);

        handler.constructKey();

        if (keys.containsKey(handler.getKey().getFingerPrint())) {
            throw new KeyAlreadyExistsException();
        }

        keys.put(handler.getKey().getFingerPrint(), handler);
        instance.getConfigurationManager().addKey(key);
    }

    public void loadKey(Key key) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyHandler handler = new KeyHandler(key);
        handler.constructKey();

        keys.put(key.getFingerPrint(), handler);
    }
}
