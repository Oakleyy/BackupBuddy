package ninja.oakley.backupbuddy.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javafx.application.Platform;
import ninja.oakley.backupbuddy.BackupBuddy;

public class RequestHandler {

    private BackupBuddy instance;

    private List<RequestThread> threads = new ArrayList<>();
    private int maxThreads = 5;

    public RequestHandler(BackupBuddy instance) {
        this.instance = instance;
    }

    public void createThreads(int amount, boolean start) {
        int created = 0;
        while (threads.size() < getMaxThreads() && created < amount) {
            RequestThread rt = createThread();

            if (start) {
                rt.start();
            }
            created++;
        }
    }

    public RequestThread createThread() {
        RequestThread rt = new RequestThread(instance);
        threads.add(rt);
        return rt;
    }

    public void addRequest(Request req) {
        addRequest(req, true);
    }

    public void addRequest(Request req, boolean show) {
        if (show) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        instance.getQueueController().addItem(req);
                    }
                });
            } else {
                instance.getQueueController().addItem(req);
            }
        }
        getLeastPopulatedThread().addRequest(req);
    }

    public RequestThread getLeastPopulatedThread() {
        List<RequestThread> sorted = new ArrayList<>(threads);
        sorted.sort(new RequestThread(null));
        RequestThread lowest = sorted.get(0);

        if (!lowest.isAlive()) {
            lowest.start();
        }

        return lowest;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void stopThread(RequestThread thread) {
        thread.setAlive(false);
    }

    public void stopAllThreads() {
        ListIterator<RequestThread> iter = threads.listIterator();
        while (iter.hasNext()) {
            RequestThread next = iter.next();
            next.setAlive(false);
        }
    }

}
