package com.studentapp.student_management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/states")
public class StateController {
    @Autowired JdbcTemplate jdbc;

    @GetMapping
    public ResponseEntity<?> list() {
        List<Map<String,Object>> s = jdbc.queryForList("SELECT id, name FROM state_name ORDER BY name");
        return ResponseEntity.ok(s);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        String name = body.get("name");
        jdbc.update("INSERT IGNORE INTO state_name (name) VALUES (?)", name);
        return ResponseEntity.ok(Map.of("status","saved"));
    }
}

