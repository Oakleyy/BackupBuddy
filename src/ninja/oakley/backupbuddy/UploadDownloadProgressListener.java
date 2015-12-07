package ninja.oakley.backupbuddy;

import java.io.IOException;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import javafx.application.Platform;
import ninja.oakley.backupbuddy.controllers.QueueScreenController;
import ninja.oakley.backupbuddy.queue.Request;

public class UploadDownloadProgressListener
        implements MediaHttpUploaderProgressListener, MediaHttpDownloaderProgressListener {

    private BackupBuddy instance;
    private Request request;

    public UploadDownloadProgressListener(Request request, BackupBuddy instance) {
        this.request = request;
        this.instance = instance;
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        double progress = uploader.getProgress();
        request.setProgress(progress);
        refresh(instance.getQueueController());
    }

    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        double progress = downloader.getProgress();
        request.setProgress(progress);
        refresh(instance.getQueueController());
    }

    private void refresh(QueueScreenController con) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                con.refresh();
            }
        });
    }
}
