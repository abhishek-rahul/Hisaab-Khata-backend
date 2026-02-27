package com.hisaab_khata.hisaab_khata.controller;
import org.springframework.web.bind.annotation.*;
@RestController
public class HealthController {
  @GetMapping("/health")
  public String health() { return "OK"; }
}
