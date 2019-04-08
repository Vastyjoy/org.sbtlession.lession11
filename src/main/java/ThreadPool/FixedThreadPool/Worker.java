package ThreadPool.FixedThreadPool;

import ThreadPool.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker  extends  Thread{
    //Флаг на продолжение работы ?возможно не нужен
    private AtomicBoolean execute;
    //Текущая задача
    private Runnable curTask;
    //Очередь задач
    private BlockingQueue<Runnable> blockingQueue;

    /**
     *
     * @param name Название воркера
     * @param aBoolean готовность к выполнению
     * @param blockingQueue очередь задач для выполнения
     */
    Worker(String name, AtomicBoolean aBoolean, BlockingQueue<Runnable> blockingQueue) {
        super(name);
        this.execute = aBoolean;
        this.blockingQueue = blockingQueue;
        this.curTask=curTask;
    }

    @Override
    public void run() {
        try {
            while (execute.get() && !isInterrupted()) {
                if(curTask!=null) curTask.run();
                curTask=blockingQueue.take();
            }
        }catch (InterruptedException run){
            System.err.println("Thread interrupt:"+getName());

        }
    }
}
