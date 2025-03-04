package com.example.jvms.service;

import com.example.jvms.entity.Person;
import com.example.jvms.repository.PersonRepository;
import com.example.jvms.service.dto.DummyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Service
public class JvmService {
  private final AtomicLong atomicCounter = new AtomicLong();
  private volatile long volatileCounter = 0;

  @Autowired
  private PersonRepository personRepository;
  @Autowired
  private StringRedisTemplate redisTemplate;

  public String testPerformance() throws Exception {
    return "Normal Loop: " + testNormalLoop() + " ms\n" +
        "Object Allocation: " + testObjectAllocation() + " ms\n" +
        "Streams Loop: " + testStreamLoop() + " ms\n" +
        "Database Performance (JPA): " + testDatabasePerformance() + " ms\n" +
        "Redis Performance: " + testRedisPerformance() + " ms\n" +
        "Atomic Var Loop: " + testAtomicVar() + " ms\n" +
        "Volatile Var Loop: " + testVolatileVar() + " ms\n" +
        "Atomic Var Loop using Threads: " + testAtomicVarUsingThreads() + " ms\n" +
        "Volatile Var Loop using Threads: " + testVolatileVarUsingThreads() + " ms\n";
  }

  private long testNormalLoop() {
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

  private long testStreamLoop() {
    long startLoop = System.nanoTime();
    int[] data = IntStream.range(0, 1_000_000).toArray();
    int sumLoop = 0;
    for (int j : data) {
      sumLoop += j;
    }
    return (System.nanoTime() - startLoop) / 1_000_000;
  }

  private long testDatabasePerformance() {
    long start = System.nanoTime();
    for (int i = 0; i < 100; i++) {
      Person insertedPerson = personRepository.saveAndFlush(new Person("User " + i, new Random().nextInt(100)));
      Optional<Person> newPerson = personRepository.findById(insertedPerson.getId());
      if (newPerson.isEmpty()) {
        System.out.println("PERSON " + insertedPerson.getId() + " IS NULL!");
      }
    }
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testRedisPerformance() {
    long start = System.nanoTime();
    for (int i = 0; i < 100; i++) {
      String key = "user:" + i;
      String value = "User " + i + "," + new Random().nextInt(100);

      // Inserir no Redis
      this.redisTemplate.opsForValue().set(key, value);

      // Ler do Redis
      String newValue = this.redisTemplate.opsForValue().get(key);

      if (newValue == null) {
        System.out.println("KEY " + key + " IS NULL!");
      }
    }
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testAtomicVar() {
    long startAtomic = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      atomicCounter.incrementAndGet();
    }
    return (System.nanoTime() - startAtomic) / 1_000_000;
  }

  private long testVolatileVar() {
    long startVolatile = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      volatileCounter++;
    }
    return (System.nanoTime() - startVolatile) / 1_000_000;
  }

  private long testAtomicVarUsingThreads() throws InterruptedException {
    atomicCounter.set(0);
    long start = System.nanoTime();
    this.runThreads(() -> atomicCounter.incrementAndGet());
    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testVolatileVarUsingThreads() throws InterruptedException {
    volatileCounter = 0;
    long start = System.nanoTime();
    this.runThreads(() -> volatileCounter++);
    return (System.nanoTime() - start) / 1_000_000;
  }

  private void runThreads(Runnable task) throws InterruptedException {
    int threads = 5;
    int increments = 100_000;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        for (int j = 0; j < increments; j++) {
          task.run();
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
  }
}
