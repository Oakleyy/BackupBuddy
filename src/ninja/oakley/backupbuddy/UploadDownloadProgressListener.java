package ninja.oakley.backupbuddy;

import java.io.IOException;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import javafx.scene.control.ProgressBar;

public class UploadDownloadProgressListener
        implements MediaHttpUploaderProgressListener, MediaHttpDownloaderProgressListener {

    private ProgressBar progressBar;

    public UploadDownloadProgressListener(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        double progress = uploader.getProgress();
        progressBar.setProgress(progress);
    }

    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        double progress = downloader.getProgress();
        progressBar.setProgress(progress);
    }

}
