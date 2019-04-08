package ThreadPool.ScalableThreadPool;

import java.time.LocalTime;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;

public class Worker extends Thread {
    //Флаг на продолжение работы ?возможно не нужен
    private AtomicBoolean execute;
    //Текущая задача
    private Runnable curTask;
    //Очередь задач
    private final BlockingQueue<Runnable> blockingQueue;
    //Время ожидания потока без задач
    private final long timeLiveCycle;
    //Время последней задачи
    private long timeLastTask;
    //Является ли поток временным
    private final boolean isTemprorary;

    /**
     * @param name          Название воркера
     * @param aBoolean      готовность к выполнению
     * @param blockingQueue очередь задач для выполнения
     */
    Worker(String name, AtomicBoolean aBoolean, BlockingQueue<Runnable> blockingQueue, boolean isTemprorary, long timeLiveCycle, Runnable curTask) {
        super(name);
        this.execute = aBoolean;
        this.timeLiveCycle = timeLiveCycle;
        this.blockingQueue = blockingQueue;
        this.curTask = curTask;
        this.isTemprorary = isTemprorary;

    }

    @Override
    public void run() {
        try {

            while (execute.get() && !isInterrupted()) {
                //Костыль спросить как обойти
                //Спим пока не разбудят только в том случае если очередь пуста.
                //Будить будут при добавлении в очередь элемента
                synchronized (blockingQueue) {
                    if (blockingQueue.isEmpty()) blockingQueue.wait(timeLiveCycle);
                }
                curTask = blockingQueue.poll();
                if (curTask != null) {
                    curTask.run();
                    timeLastTask = System.currentTimeMillis();

                } else
                    if (isTemprorary && (System.currentTimeMillis() - timeLastTask) > timeLiveCycle) {
                    if (execute.get()) System.err.println("Thread interrupt livecycle:" + getName());
                    interrupt();
                }


            }
        } catch (InterruptedException run) {
            run.printStackTrace();
            System.err.println("Thread interrupt because threadpool shutdown");

        }
    }
}
