package org.hoi.various;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Threading extends Thread {
    final public static int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    final public int length;

    private AtomicInteger pos;

    public Threading (int length) {
        super();
        this.length = length;
    }

    public abstract void epoch (int pos);

    @Override
    public void run() {
        Thread[] threads = new Thread[MAX_THREADS];
        boolean[] done = new boolean[length];

        pos = new AtomicInteger();
        for (int i=0;i<threads.length;i++) {
            threads[i] = new Thread(() -> {
                int index;
                while ((index = pos.getAndIncrement()) < length) {
                    try {
                        epoch(index);
                        done[index] = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            threads[i].start();
        }

        while (true) {
            if (Arrays.stream(threads).noneMatch(Thread::isAlive)) {
                break;
            }
        }

        // VERIFY ALL EPOCHS WERE EXECUTED
        for (int i=0;i<length;i++) {
            if (!done[i]) {
                epoch(i);
            }
        }
    }

    public float getProgress () {
        return (float) pos.get() / length;
    }
}
