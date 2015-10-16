package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import javafx.scene.control.ProgressBar;

public class UploadProgressListener implements MediaHttpUploaderProgressListener {

	private ProgressBar progressBar;
	
	public UploadProgressListener(ProgressBar progressBar){
		this.progressBar = progressBar;
	}
	
	@Override
	public void progressChanged(MediaHttpUploader uploader) throws IOException {
		double progress = uploader.getProgress();
		progressBar.setProgress(progress);
	}

}
