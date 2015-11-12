package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import javafx.scene.control.ProgressBar;
import ninja.oakley.backupbuddy.controllers.UploadDownloadProgressListener;

public class UploadRequest implements Request {

	private BucketManager manager;
	private Path filePath;
	private String bucketName;
	private ProgressBar bar;
	
	public UploadRequest(BucketManager manager, Path filePath, String bucketName){
		this.manager = manager;
		this.filePath = filePath;
		this.bucketName = bucketName;
	}

	public Path getFilePath(){
		return this.filePath;
	}
	
	public File getFile() {
		return this.filePath.toFile();
	}
	
	public String getBucketName(){
		return this.bucketName;
	}
	
	public String getContentType() throws IOException {
		return Files.probeContentType(filePath);
	}
	
	public BucketManager getBucketManager(){
		return manager;
	}
	
	public void setProgressBar(ProgressBar bar){
		this.bar = bar;
	}
	
	public void execute() throws IOException, GeneralSecurityException{
		manager.uploadStream(getBucketName(), 
				getFile().getName(), 
				getContentType(), 
				getFile(), 
				new UploadDownloadProgressListener(bar));
	}
	
}
