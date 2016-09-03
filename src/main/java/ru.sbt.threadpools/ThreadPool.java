package ru.sbt.threadpools;

public interface ThreadPool {
    void start();

    void execute(Runnable runnable);
}
