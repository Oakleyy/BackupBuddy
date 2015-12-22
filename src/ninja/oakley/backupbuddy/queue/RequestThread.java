package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ninja.oakley.backupbuddy.BackupBuddy;

public class RequestThread extends Thread implements Comparator<RequestThread> {

    private static final Logger logger = LogManager.getLogger(RequestThread.class);
    private BackupBuddy instance;

    private BlockingQueue<Request> queue;
    private volatile boolean alive = false;

    public RequestThread(BackupBuddy instance) {
        setDaemon(true);
        this.instance = instance;
        queue = new LinkedBlockingQueue<Request>();
    }

    @Override
    public void run() {
        alive = true;

        while (alive) {
            try {
                Request req = queue.take();

                req.execute(instance);
            } catch (InterruptedException e) {
                alive = false;
                return;
            } catch (IOException e) {
                logger.error("File not found." + e);
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

    @Override
    public int compare(RequestThread o1, RequestThread o2) {
        return o1.getQueueLength() - o2.getQueueLength();
    }

}