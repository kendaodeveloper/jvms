package com.example.jvms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "person")
public class Person {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private Integer age;

  public Person() {
  }

  public Person(String name, Integer age) {
    this.name = name;
    this.age = age;
  }

  public Long getId() {
    return this.id;
  }
}
