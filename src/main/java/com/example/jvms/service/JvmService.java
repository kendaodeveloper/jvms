package com.example.jvms.service;

import com.example.jvms.entity.Person;
import com.example.jvms.repository.PersonRepository;
import com.example.jvms.service.dto.DummyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
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
        "Streams Loop: " + testStreamLoop() + " ms\n" +
        "Object Allocation: " + testObjectAllocation() + " ms\n" +
        "Database Performance (JPA): " + testDatabasePerformance() + " ms\n" +
        "Redis Performance: " + testRedisPerformance() + " ms\n" +
        "Atomic Var Loop: " + testAtomicVar() + " ms\n" +
        "Volatile Var Loop: " + testVolatileVar() + " ms\n" +
        "Atomic Var Loop using Threads: " + testAtomicVarUsingThreads() + " ms\n" +
        "Volatile Var Loop using Threads: " + testVolatileVarUsingThreads() + " ms\n" +
        "String Concatenation: " + testStringConcatenation() + " ms\n" +
        "File IO Performance : " + testFileIOPerformance() + " ms\n" +
        "Collections Performance : " + testCollectionsPerformance() + " ms\n";
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

  private long testStringConcatenation() {
    long start = System.nanoTime();

    String str = "";
    for (int i = 0; i < 10000; i++) {
      str += "a";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append("a");
    }

    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testCollectionsPerformance() {
    long start = System.nanoTime();

    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();
    List<Integer> vector = new Vector<>(); // thread-safe

    Set<Integer> hashSet = new HashSet<>();
    Set<Integer> linkedHashSet = new LinkedHashSet<>();
    Set<Integer> treeSet = new TreeSet<>();
    Set<Integer> copyOnWriteSet = new CopyOnWriteArraySet<>(); // thread-safe

    Map<Integer, Integer> hashMap = new HashMap<>();
    Map<Integer, Integer> linkedHashMap = new LinkedHashMap<>();
    Map<Integer, Integer> treeMap = new TreeMap<>();
    Map<Integer, Integer> hashTable = new Hashtable<>(); // thread-safe
    Map<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>(); // thread-safe

    int dataSize = 100_000;

    // Teste de inserção
    long insertStart = System.nanoTime();

    for (int i = 0; i < dataSize; i++) {
      arrayList.add(i);
      linkedList.add(i);
      vector.add(i);

      hashSet.add(i);
      linkedHashSet.add(i);
      treeSet.add(i);
      copyOnWriteSet.add(i);

      hashMap.put(i, i);
      linkedHashMap.put(i, i);
      treeMap.put(i, i);
      hashTable.put(i, i);
      concurrentHashMap.put(i, i);
    }
    long insertTime = (System.nanoTime() - insertStart) / 1_000_000;

    // Teste de iteração
    long iterateStart = System.nanoTime();

    for (Integer i : arrayList) {
    }
    for (Integer i : linkedList) {
    }
    for (Integer i : vector) {
    }

    for (Integer i : hashSet) {
    }
    for (Integer i : linkedHashSet) {
    }
    for (Integer i : treeSet) {
    }
    for (Integer i : copyOnWriteSet) {
    }

    for (Integer i : hashMap.keySet()) {
    }
    for (Integer i : linkedHashMap.keySet()) {
    }
    for (Integer i : treeMap.keySet()) {
    }
    for (Integer i : hashTable.keySet()) {
    }
    for (Integer i : concurrentHashMap.keySet()) {
    }

    long iterateTime = (System.nanoTime() - iterateStart) / 1_000_000;

    return (System.nanoTime() - start) / 1_000_000;
  }

  private long testFileIOPerformance() throws IOException {
    long start = System.nanoTime();
    Path path = Paths.get("testfile.txt");

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      for (int i = 0; i < 100000; i++) {
        writer.write("Sample data line " + i + "\n");
      }
    }

    try (BufferedReader reader = Files.newBufferedReader(path)) {
      while (reader.readLine() != null) {
      }
    }

    Files.delete(path);
    return (System.nanoTime() - start) / 1_000_000;
  }
}
