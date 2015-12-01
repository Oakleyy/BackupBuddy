package ninja.oakley.backupbuddy;

import java.io.IOException;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import ninja.oakley.backupbuddy.queue.Request;

public class UploadDownloadProgressListener implements MediaHttpUploaderProgressListener, MediaHttpDownloaderProgressListener {

    private Request request;    
    
    public UploadDownloadProgressListener(Request request) {
        this.request = request;
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        double progress = uploader.getProgress();
        request.setProgress(progress);
    }

    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        double progress = downloader.getProgress();
        request.setProgress(progress);
    }
}
