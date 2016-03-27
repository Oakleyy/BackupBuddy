package ninja.oakley.backupbuddy.encryption;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;

public class KeyHandler {

    private final static String ALGORITHM = "RSA";

    private Key key;
    private RSAPrivateCrtKey rsa;

    public KeyHandler(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public InputStream encryptStream(InputStream stream)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsa);

        return new CipherInputStream(stream, cipher);
    }

    public InputStream decryptStream(InputStream stream)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsa);

        return new CipherInputStream(stream, cipher);
    }

    public void constructKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        rsa = loadPrivateKeyFromFile(Paths.get(key.getKeyPath()));
        key.setFingerPrint(generateFingerPrint(rsa));
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
    
    @Override
    public String toString(){
        return key.toString();
    }
}
