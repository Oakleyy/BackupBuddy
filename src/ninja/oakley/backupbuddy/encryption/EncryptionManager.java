package ninja.oakley.backupbuddy.encryption;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Hex;

public class EncryptionManager {

    private final static String ALGORITHM = "RSA";

    private ConcurrentHashMap<String, KeyHandler> keys = new ConcurrentHashMap<>();

    public EncryptionManager() {

    }

    public KeyHandler getKeyHandler(String fingerPrint) {
        return keys.get(fingerPrint);
    }

    public KeyHandler addKey(Path path)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyAlreadyExistsException {
        RSAPrivateCrtKey key = loadPrivateKeyFromFile(path);
        String fingerPrint = generateFingerPrint(key);

        if (keys.containsKey(fingerPrint)) {
            throw new KeyAlreadyExistsException();
        }

        KeyHandler handler = new KeyHandler(key, path, fingerPrint);
        keys.put(fingerPrint, handler);
        return handler;
    }

    private RSAPrivateCrtKey loadPrivateKeyFromFile(Path path)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File file = path.toFile();
        FileInputStream fileInput = new FileInputStream(file);
        DataInputStream dataInput = new DataInputStream(fileInput);

        byte[] keyBytes = new byte[(int) file.length()];
        dataInput.readFully(keyBytes);
        dataInput.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

        return (RSAPrivateCrtKey) kf.generatePrivate(spec);

    }

    @SuppressWarnings("unused")
    private PublicKey loadPublicKeyFromFile(Path path)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File file = path.toFile();
        FileInputStream fileInput = new FileInputStream(file);
        DataInputStream dataInput = new DataInputStream(fileInput);

        byte[] keyBytes = new byte[(int) file.length()];
        dataInput.readFully(keyBytes);
        dataInput.close();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        return kf.generatePublic(spec);
    }

    private String generateFingerPrint(RSAPrivateCrtKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        MessageDigest md = MessageDigest.getInstance("MD5");

        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        byte[] hash = md.digest(publicKey.getEncoded());

        String hexUnread = new String(Hex.encodeHex(hash));

        return hexUnread.replaceAll("(.{2})(?!$)", "$1:");
    }

}
