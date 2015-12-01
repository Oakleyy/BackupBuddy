package ninja.oakley.backupbuddy.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class RequestManager {

    private List<RequestThread> threads = new ArrayList<>();
    private int maxThreads = 5;

    public RequestManager() {
        
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
        RequestThread rt = new RequestThread();
        threads.add(rt);
        return rt;
    }

    public void addRequest(Request req) {
        getLeastPopulatedThread().addRequest(req);
    }
    
    public RequestThread getLeastPopulatedThread() {
        List<RequestThread> sorted = new ArrayList<>(threads);
        sorted.sort(new RequestThread());
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
