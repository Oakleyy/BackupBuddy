package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.ProgressBar;
import ninja.oakley.backupbuddy.BackupBuddy;

public class RequestThread extends Thread {

    private static final Logger logger = LogManager.getLogger(RequestThread.class);

    private BackupBuddy instance;

    private BlockingQueue<Request> queue;
    private volatile boolean alive = false;

    public RequestThread(BackupBuddy instance) {
        this.instance = instance;
        queue = new LinkedBlockingQueue<Request>();
    }

    @Override
    public void run() {
        alive = true;
        ProgressBar bar = instance.getBaseController().progressBar;

        while (alive) {
            try {
                Request req = queue.take();
                req.setProgressBar(bar);

                req.execute();
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

    public int getQueueLength() {
        return queue.size();
    }

    public void addRequest(Request req) {
        queue.add(req);
    }

    public boolean isRunning() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

}