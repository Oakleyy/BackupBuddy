package ninja.oakley.backupbuddy.queue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.UploadDownloadProgressListener;
import ninja.oakley.backupbuddy.project.ProjectController;

public class UploadRequest implements Request {

    private ProjectController controller;
    private Path filePath;
    private String bucketName;
    private double progress;

    public UploadRequest(ProjectController controller, Path filePath, String bucketName) {
        this.controller = controller;
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

    public ProjectController getBucketManager() {
        return controller;
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
        controller.uploadObject(getBucketName(), getFile().getName(), getContentType(), getFile(),
                new UploadDownloadProgressListener(this, instance));
    }

    @Override
    public String toString() {
        return filePath.toFile().getName();
    }

}
