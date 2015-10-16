package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.ProgressBar;
import ninja.oakley.backupbuddy.controllers.UploadProgressListener;

public class UploadThread extends Thread {

	private static final Logger logger = LogManager.getLogger(UploadThread.class);
	
	private BucketManager manager;
	private BackupBuddy instance;

	private BlockingQueue<UploadRequest> uploadQueue;
	private boolean alive = false;

	public UploadThread(BackupBuddy instance, BucketManager manager){
		this.instance = instance;
		this.manager = manager;
		this.uploadQueue = new LinkedBlockingQueue<UploadRequest>();
	}

	@Override
	public void run(){
		alive = true;
		ProgressBar bar = instance.getBaseController().progressBar;

		while(alive){

			try {
				UploadRequest req = uploadQueue.take();
				File file = req.getFile();

				manager.uploadStream(req.getBucketName(), 
						file.getName(), 
						req.getContentType(), 
						file, 
						new UploadProgressListener(bar));
				
				
			} catch (InterruptedException e) {
				alive = false;
				return;
			} catch (IOException e) {
				logger.error("File not found.");
			} catch (GeneralSecurityException e) {
				logger.error("Error while uploading file.");
			}
		}
	}

	public void addUploadRequest(UploadRequest req){
		uploadQueue.add(req);
	}
	
	public boolean isRunning(){
		return this.alive;
	}

}