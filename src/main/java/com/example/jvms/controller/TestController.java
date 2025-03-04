package com.example.jvms.controller;

import com.example.jvms.service.JvmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/jvm")
public class TestController {
  @Autowired
  private JvmService jvmService;

  @GetMapping("/info")
  public Map<String, String> getJvmInfo() {
    Map<String, String> info = new HashMap<>();
    info.put("java.version", System.getProperty("java.version"));
    info.put("java.vendor", System.getProperty("java.vendor"));
    info.put("java.vm.name", System.getProperty("java.vm.name"));
    info.put("java.vm.version", System.getProperty("java.vm.version"));
    info.put("java.vm.vendor", System.getProperty("java.vm.vendor"));
    info.put("java.compiler", System.getProperty("java.compiler") == null ? "JIT" : System.getProperty("java.compiler"));
    return info;
  }

  @GetMapping("/test")
  public String testJvm() {
    return this.jvmService.testPerformance();
  }
}
