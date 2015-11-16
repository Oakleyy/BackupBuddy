package ninja.oakley.backupbuddy.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ninja.oakley.backupbuddy.BackupBuddy;

public class RequestManager {

    private BackupBuddy instance;

    private List<RequestThread> threads = new ArrayList<>();
    private int maxThreads = 2;

    public RequestManager(BackupBuddy instance) {
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
        getLeastPopulatedThread().addRequest(req);
    }

    public RequestThread getLeastPopulatedThread() {
        ListIterator<RequestThread> iter = threads.listIterator();
        RequestThread lowest = null;

        while (iter.hasNext()) {
            RequestThread next = iter.next();
            int amount = next.getQueueLength();

            if (amount == 0) {
                lowest = next;
                break;
            }

            if (lowest == null || amount < lowest.getQueueLength()) {
                lowest = next;
            }
        }

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
