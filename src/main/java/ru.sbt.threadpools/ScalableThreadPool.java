package ru.sbt.threadpools;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ScalableThreadPool implements ThreadPool {
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final int minThreads;
    private final int maxThreads;
    private volatile AtomicInteger currentThreads;
    private final List<EndlessThread> runningThreads = new ArrayList<>();

    public ScalableThreadPool(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        currentThreads = new AtomicInteger(minThreads);
    }

    @Override
    public void start() {
        for (int i = 0; i < minThreads; i++) {
            runningThreads.add(new EndlessThread());
            runningThreads.get(i).start();
        }
    }

    @Override
    public void execute(Runnable r) {
        tasks.add(r);
        boolean isThereFreeThreads = false;
        for (EndlessThread thread : runningThreads) {
            if(!thread.isRunning){
                isThereFreeThreads = true;
                break;
            }
        }

        if (!isThereFreeThreads && currentThreads.get() < maxThreads){
            new TemporaryThread().start();
            currentThreads.addAndGet(1);
        }
    }

    private class EndlessThread extends Thread {
        private volatile boolean isRunning = false;

        @Override
        public void run() {
            while (true) {
                if (!tasks.isEmpty()) {
                    Runnable r = prepare();
                    run(r);
                }
            }
        }

        private void run(Runnable r) {
            isRunning = true;
            r.run();
            isRunning = false;
        }

        private Runnable prepare() {
            Runnable r;
            synchronized (tasks) {
                if (tasks.isEmpty()) {
                    try {
                        tasks.wait();
                    } catch (InterruptedException ignore) {
                    }
                }
                r = tasks.poll();
            }
            return r;
        }
    }

    private class TemporaryThread extends Thread{
        @Override
        public void run() {
            while (!tasks.isEmpty()){
                Runnable r = null;
                synchronized (tasks){
                    if (!tasks.isEmpty()){
                        r = tasks.poll();
                    }
                }
                if(!(r == null)) r.run();
            }
            currentThreads.addAndGet(-1);
        }
    }
}