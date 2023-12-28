package org.example.pool;

// дефолтный модификатор доступа, чтобы класс был виден только в пакете
class WorkingThread extends Thread {
    private ThreadPool threadPool;

    private boolean isLastJobFinishedOk = true;

    public WorkingThread(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        // как только тред завелся - код сразу же начинает пытаться запросить у менеджера джобу для выполнения
        while (true) {
            Runnable job = threadPool.getNextJobForExecution();
            if(job == null) {
                return;
            }
            try {
                job.run();
                // код попадет сюда только если метод не выкенет runtime exception
                isLastJobFinishedOk = true;
            } catch (RuntimeException e) {
                isLastJobFinishedOk = false;
            }
        }
    }

    // ну и геттер для флага, причем без сеттера - чтобы программист своими грязными рученками не пошел менять то,
    // его не надо менять
    public boolean isLastJobFinishedOk() {
        return isLastJobFinishedOk;
    }
}
