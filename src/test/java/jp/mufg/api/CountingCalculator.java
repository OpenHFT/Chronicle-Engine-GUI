package jp.mufg.api;

import java.util.concurrent.atomic.AtomicLong;

public class CountingCalculator implements Calculator {
    final AtomicLong counter = new AtomicLong();

    public long getCount() {
        return counter.get();
    }

    @Override
    public void calculate() {
        counter.incrementAndGet();
    }
}
