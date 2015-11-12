package ninja.oakley.backupbuddy;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import javafx.scene.control.ProgressBar;
import ninja.oakley.backupbuddy.controllers.UploadDownloadProgressListener;

public class DownloadRequest implements Request {
	
	private BucketManager manager;
	private Path savePath;
	private String file;
	private String bucketName;
	private ProgressBar bar;
	
	public DownloadRequest(BucketManager manager, String file, Path savePath, String bucketName){
		this.manager = manager;
		this.file = file;
		this.savePath = savePath;
		this.bucketName = bucketName;
	}

	@Override
	public void execute() throws IOException, GeneralSecurityException {
		manager.downloadStream(bucketName, file, savePath, new UploadDownloadProgressListener(bar));
		
	}

	@Override
	public void setProgressBar(ProgressBar bar) {
		this.bar = bar;
	}

}
