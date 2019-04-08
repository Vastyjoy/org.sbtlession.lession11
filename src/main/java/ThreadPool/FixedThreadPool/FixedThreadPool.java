package ThreadPool.FixedThreadPool;

import ThreadPool.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixedThreadPool implements ThreadPool {
    private BlockingQueue<Runnable> runnables;
    private AtomicBoolean execute;
    private int poolCount;
    private List<Worker> workers;

    class ThreadPoolException extends RuntimeException {
        public ThreadPoolException() {
        }

        public ThreadPoolException(String message) {
            super(message);
        }

        public ThreadPoolException(String message, Throwable cause) {
            super(message, cause);
        }

        public ThreadPoolException(Throwable cause) {
            super(cause);
        }

        public ThreadPoolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * @param poolCount количество потоков, которое необходимо создать в пуле
     */
    private FixedThreadPool(int poolCount) {
        this.poolCount = poolCount;
        this.runnables = new LinkedBlockingQueue<>();
        this.execute = new AtomicBoolean(true);
        this.workers = new ArrayList<>();
        for (int i = 0; i < this.poolCount; i++) {
            Worker worker = new Worker("WorkerFTP:" + i, execute, runnables);
            workers.add(worker);
        }
    }
    /**
     * Проверяем какие потоки в списке еще живы, мертвые потоки вычищаем.
     */
    private void checkAlive(){
        for (Worker worker : workers) {
            if (!worker.isAlive()) workers.remove(worker);
        }
    }
    /**
     * Получить новый пул потоков, размер которого равен количеству процессоров текущей системы
     *
     * @return
     */
    public static FixedThreadPool getInstance() {
        return getInstance(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Получить новый пул потоков с заданным размером
     *
     * @param poolCount количество потоков в новом пуле потоков.
     * @return
     */
    public static FixedThreadPool getInstance(int poolCount) {
        return new FixedThreadPool(poolCount);
    }

    /**
     * запускает потоки. Потоки бездействуют, до тех пор пока не появится новое задание в очереди (см. execute)
     */
    public void start() {
        for(Worker worker:workers){
            worker.start();
        }
    }

    /**
     * Добавляет задание к выполнению
     *
     * @param runnable задача которую необходимо выполнить одному из потоков
     */
    public void execute(Runnable runnable) {
        if (!execute.get()) new IllegalArgumentException("Threadpool terminating, unable to execute runnable");
        if(runnable!=null)
        runnables.add(runnable);
    }
    public void terminate(){
        execute.set(false);
        for(Worker worker:workers){
            worker.interrupt();
        }
        workers.clear();
    }

}
