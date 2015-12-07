package ninja.oakley.backupbuddy.queue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.UploadDownloadProgressListener;
import ninja.oakley.backupbuddy.project.BucketManager;

public class UploadRequest implements Request {

    private BucketManager manager;
    private Path filePath;
    private String bucketName;
    private double progress;

    public UploadRequest(BucketManager manager, Path filePath, String bucketName) {
        this.manager = manager;
        this.filePath = filePath;
        this.bucketName = bucketName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public File getFile() {
        return filePath.toFile();
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getContentType() throws IOException {
        return Files.probeContentType(filePath);
    }

    public BucketManager getBucketManager() {
        return manager;
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
    public void execute(BackupBuddy instance) throws IOException, GeneralSecurityException {
        manager.uploadStream(getBucketName(), getFile().getName(), getContentType(), getFile(),
                new UploadDownloadProgressListener(this, instance));
    }

    @Override
    public String toString() {
        return filePath.toFile().getName();
    }

}
