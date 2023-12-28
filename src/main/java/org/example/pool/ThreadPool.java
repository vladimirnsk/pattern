package org.example.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    public ThreadPool(int nJobs) {
        workingThreads = new ArrayList<>(nJobs);
        for (int i = 0; i < nJobs; i++) {
            var thread = new WorkingThread(this);
            thread.setDaemon(false);
            workingThreads.add(thread);
        }
        workingThreads.forEach(WorkingThread::start);
    }

    private final LinkedBlockingQueue<Runnable> jobsList = new LinkedBlockingQueue<>();
    private final List<WorkingThread> workingThreads;

    public void execute(Runnable job) {
        synchronized (jobsList) {
            jobsList.add(job);
            jobsList.notify();
        }
    }

    protected boolean isJoining = false;

    public void joinPool() throws InterruptedException {
        isJoining = true;
        for (var t : workingThreads) {
            t.join();
        }
    }

    /*
    было решено вынести этот код сюда, чтобы working thread не знал, каким образом работает под капотом pool
     */
    protected Runnable getNextJobForExecution() {
        synchronized (jobsList) {
            /*
            т.к. https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#wait--
            wait и notify - обертки низкоуровневых вызовов от api os, из-за чего приходится писать while с условием,
            поскольку бывают ошибки пропущенного пробуждения (notify вызвался, а вот wait не отстрелился)
             */
            while (jobsList.isEmpty()) {
                if(isJoining) {
                    return null;
                }
                try {
                    jobsList.wait(10);
                } catch (InterruptedException ignored) {
                    /*
                    как следует из документации Object - у метода wait вызывается interruptedException в случае
                    если у треда, где выполняется код будет вызван метод interrupt.
                    В нашем случае такого быть не должно, но чтобы не отлавливать исключение потом - сделаем этот тут
                    и не будем ничего делать
                     */
                }
            }
            return jobsList.poll();
        }
    }
}
