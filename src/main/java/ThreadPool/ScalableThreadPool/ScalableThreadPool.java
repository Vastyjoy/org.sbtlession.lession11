package ThreadPool.ScalableThreadPool;

import ThreadPool.ThreadPool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScalableThreadPool implements ThreadPool {

    private BlockingQueue<Runnable> runnables;
    private AtomicBoolean execute;
    private long timeLiveCycleThread;
    private int minPoolCount;
    private int maxPoolCount;
    private List<Worker> workers;
    private boolean isStart;

    /**
     * @param minPoolCount минимальное количество потоков в пуле
     * @param maxPoolCount максимальное количество потоков в пуле
     */
    private ScalableThreadPool(int minPoolCount, int maxPoolCount, long timeLiveCycleThread) {
        if (minPoolCount > maxPoolCount) throw new IllegalArgumentException("MinPoolCount>MaxPoolCount");
        this.timeLiveCycleThread = timeLiveCycleThread;
        this.minPoolCount = minPoolCount;
        this.maxPoolCount = maxPoolCount;
        this.runnables = new LinkedBlockingQueue<>();
        this.execute = new AtomicBoolean(true);
        this.workers = new CopyOnWriteArrayList<>();
        this.isStart = false;

        for (int i = 0; i < this.minPoolCount; i++) {
            Worker worker = new Worker("WorkerSTP notTemprorary:" + workers.size(), execute, runnables, false, timeLiveCycleThread, null);
            workers.add(worker);
        }
    }

    /**
     * Проверяем какие потоки в списке еще живы, мертвые потоки вычищаем.
     */
    private void checkAlive() {
        for (Worker worker : workers) {
            if ((worker.getState() != Thread.State.NEW))
                if (!worker.isAlive()) workers.remove(worker);
        }
    }

    private void createAndStartTemproraryThread(Runnable runnable) {
        Worker worker = new Worker("WorkerSTP Temprorary:" + workers.size(), execute, runnables, true, timeLiveCycleThread, null);
        workers.add(worker);
        runnables.add(runnable);
        worker.start();
    }
    /**
     * Получить новый пул потоков с количеством от minPoolCount до количества процессоров  текущей системе
     *
     * @param minPoolCount минимальная граница
     * @return
     */

    /**
     * Получить новый пул потоков с заданным диапозоном количества потоков
     *
     * @param minPoolCount минимальное количество потоков в новом пуле потоков.
     * @param maxPoolCount максимальное количество потоков в новом пуле потоков.
     * @return
     */
    public static ScalableThreadPool getInstance(int minPoolCount, int maxPoolCount, long timeLiveCycleThread) {
        return new ScalableThreadPool(minPoolCount, maxPoolCount, timeLiveCycleThread);
    }

    @Override
    public void start() {
        for (Worker worker : workers) {
            if (worker.getState() == Thread.State.NEW) worker.start();
        }
        isStart = true;
    }


    @Override
    public void execute(Runnable runnable) {

        checkAlive();
        if (runnable == null) return;

        if (isStart && workers.size() != maxPoolCount) {
            createAndStartTemproraryThread(runnable);
        } else runnables.add(runnable);
        synchronized (runnables) {
            runnables.notifyAll();
        }
    }

    @Override
    public void terminate() {
        execute.set(false);
        for (Worker worker : workers) {
            worker.interrupt();
        }
        workers.clear();
    }
}
