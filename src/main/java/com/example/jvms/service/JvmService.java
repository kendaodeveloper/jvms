package com.example.jvms.service;

import com.example.jvms.entity.Person;
import com.example.jvms.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Service
public class JvmService {
  private final ConcurrentHashMap<Integer, String> concurrentMap = new ConcurrentHashMap<>();
  private final AtomicLong atomicCounter = new AtomicLong();
  @Autowired
  private PersonRepository personRepository;
  private volatile long volatileCounter = 0;

  public String testPerformance() {

    String sb = "Loop Optimization: " + testLoopOptimization() + " ms\n" +
        "Object Allocation: " + testObjectAllocation() + " ms\n" +
        "Streams vs Loops: " + testStreamVsLoop() + " ms\n" +
        "Database Performance (JPA vs JDBC): " + testDatabasePerformance() + " ms\n" +
        "Volatile vs Atomic: " + testVolatileVsAtomic() + " ms\n";

    return sb;
  }

  private long testLoopOptimization() {
    long start = System.nanoTime();
    int sum = 0;
    for (int i = 0; i < 10_000_000; i++) {
      sum += i;
    }
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testObjectAllocation() {
    long start = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      new DummyObject(i);
    }
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testStreamVsLoop() {
    int[] data = IntStream.range(0, 1_000_000).toArray();

    long startLoop = System.nanoTime();
    int sumLoop = 0;
    for (int j : data) {
      sumLoop += j;
    }
    long loopTime = (System.nanoTime() - startLoop) / 1_000_000;

    long startStream = System.nanoTime();
    int sumStream = IntStream.of(data).sum();
    long streamTime = (System.nanoTime() - startStream) / 1_000_000;

    return loopTime - streamTime; // Diferença de performance
  }

  private long testDatabasePerformance() {
    long start = System.nanoTime();
    for (int i = 0; i < 100; i++) {
      personRepository.saveAndFlush(new Person("User " + i, new Random().nextInt(100)));
    }
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testVolatileVsAtomic() {
    long startAtomic = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      atomicCounter.incrementAndGet();
    }
    long atomicTime = (System.nanoTime() - startAtomic) / 1_000_000;

    long startVolatile = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      volatileCounter++;
    }
    long volatileTime = (System.nanoTime() - startVolatile) / 1_000_000;

    return volatileTime - atomicTime;
  }

  static class DummyObject {
    int value;

    DummyObject(int value) {
      this.value = value;
    }
  }
}
