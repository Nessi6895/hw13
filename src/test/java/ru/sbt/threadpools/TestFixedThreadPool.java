package ru.sbt.threadpools;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestFixedThreadPool {
    @Test
    public void testLogs() throws InterruptedException {
        FixedThreadPool pool = new FixedThreadPool(3);
        pool.start();
        for (int i = 0; i < 100; i++) {
             pool.execute(()->{
                 throw new RuntimeException();
             });
        }
        Thread.sleep(1000); //i know it's terrible move, but i don't know how to do it properly
        assertEquals(100, pool.getLogs().size());
    }
}
