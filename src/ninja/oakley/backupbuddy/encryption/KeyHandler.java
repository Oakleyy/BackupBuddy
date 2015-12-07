package ninja.oakley.backupbuddy.encryption;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

public class KeyHandler {

    private RSAPrivateCrtKey key;
    private Path keyPath;
    private String fingerPrint;

    public KeyHandler(RSAPrivateCrtKey key, Path keyPath, String fingerPrint) {
        this.key = key;
        this.keyPath = keyPath;
        this.fingerPrint = fingerPrint;
    }

    public InputStream encryptStream(InputStream stream)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new CipherInputStream(stream, cipher);
    }

    public InputStream decryptStream(InputStream stream)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return new CipherInputStream(stream, cipher);
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public Path getKeyPath() {
        return keyPath;
    }

}
