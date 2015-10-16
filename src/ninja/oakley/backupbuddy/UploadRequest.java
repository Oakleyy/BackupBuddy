package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadRequest {

	private Path filePath;
	private String bucketName;
	
	public UploadRequest(Path filePath, String bucketName){
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
	
	
}
