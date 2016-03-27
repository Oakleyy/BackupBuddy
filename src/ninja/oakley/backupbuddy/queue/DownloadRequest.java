package ninja.oakley.backupbuddy.queue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import com.google.api.client.util.IOUtils;
import com.google.api.services.storage.model.StorageObject;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.encryption.KeyHandler;
import ninja.oakley.backupbuddy.project.ProjectController;

public class DownloadRequest implements Request {

    private ProjectController controller;
    private Path savePath;
    private String file;
    private String bucketName;
    private double progress;

    public DownloadRequest(ProjectController controller, String file, Path savePath, String bucketName) {
        this.controller = controller;
        this.file = file;
        this.savePath = savePath;
        this.bucketName = bucketName;
    }

    @Override
    public void execute(BackupBuddy instance) throws IOException, GeneralSecurityException {
        
        if (file.endsWith("/")) {
            Files.createDirectories(savePath);
            return;
        }
        
        String fingerPrint = controller.getObjectMetadata(bucketName, file).getMetadata().get("fingerprint");
        InputStream in;
        if(fingerPrint != null){
            KeyHandler handler = instance.getEncryptionManager().getKeyHandler(fingerPrint);
            in = handler.decryptStream(controller.downloadObject(bucketName, file, new UploadDownloadProgressListener(this, instance)));
        } else {
            in = controller.downloadObject(bucketName, file, new UploadDownloadProgressListener(this, instance));
        }
        
        Files.createDirectories(savePath.getParent());
        
        FileOutputStream out = new FileOutputStream(savePath.toFile());
        IOUtils.copy(in,out);
        
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        String rt = file;
        if (rt.contains("/")) {
            rt = rt.substring(rt.lastIndexOf("/") + 1, rt.length());
        }
        return rt;
    }
}
