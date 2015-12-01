package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import ninja.oakley.backupbuddy.UploadDownloadProgressListener;
import ninja.oakley.backupbuddy.project.BucketManager;

public class DownloadRequest implements Request {

    private BucketManager manager;
    private Path savePath;
    private String file;
    private String bucketName;
    private double progress;

    public DownloadRequest(BucketManager manager, String file, Path savePath, String bucketName) {
        this.manager = manager;
        this.file = file;
        this.savePath = savePath;
        this.bucketName = bucketName;
    }

    @Override
    public void execute() throws IOException, GeneralSecurityException {
        manager.downloadStream(bucketName, file, savePath, new UploadDownloadProgressListener(this));

    }
    
    @Override
    public double getProgress(){
        return this.progress;
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
    }

}
