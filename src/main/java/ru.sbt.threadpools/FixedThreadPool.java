package ru.sbt.threadpools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FixedThreadPool {
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final int nThreads;
    private final List<Exception> logs = new ArrayList<>();

    public FixedThreadPool(int nThreads) {
        this.nThreads = nThreads;
    }

    public void start(){
        for (int i = 0; i < nThreads; i++) {
             new CustomThread().start();
        }
    }

    public void execute(Runnable r){
        synchronized (tasks){
            tasks.add(r);
            tasks.notify();
        }
    }

    public List<Exception> getLogs(){
        return logs;
    }

    private class CustomThread extends Thread{
        @Override
        public void run() {
            while(true){
                Runnable r;
                synchronized (tasks){
                    while(tasks.isEmpty())
                        try {
                            tasks.wait();
                        } catch (InterruptedException ignore) {
                        }
                    r = tasks.poll();
                }
                try { r.run(); }
                catch (Exception e){ logs.add(e); }
            }
        }
    }
}
