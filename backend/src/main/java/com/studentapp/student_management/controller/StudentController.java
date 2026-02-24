package com.studentapp.student_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.studentapp.student_management.services.StudentService;
import com.studentapp.student_management.services.StudentService.PhotoBlob;

import java.io.*;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService service;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Map<String, Object>> data = service.listStudents(page, size);
        int total = service.getTotalStudentsCount();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Integer.toString(total));

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudent(@PathVariable int id) {
        Map<String, Object> result = service.getStudentById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createStudent(
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam(required = false) Date dob,
            @RequestParam String address,
            @RequestParam(required = false) Integer stateId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, name = "subjects") List<String> subjects,
            @RequestPart(required = false, name = "photos") List<MultipartFile> photos) {
        try {
            int id = service.createStudent(name, age, dob, address, stateId, phone, subjects, photos);
            return ResponseEntity.ok(Map.of("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not create student"));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable int id,
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam Date dob,
            @RequestParam String address,
            @RequestParam(required = false) Integer stateId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, name = "subjects") List<String> subjects,
            @RequestPart(required = false, name = "photos") List<MultipartFile> photos) {
        try {
            service.updateStudent(id, name, age, dob, address, stateId, phone, subjects, photos);
            return ResponseEntity.ok(Map.of("status", "updated"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update student"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable int id) {
        try {
            service.deleteStudent(id);
            return ResponseEntity.ok(Map.of("status", "deleted"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not delete student"));
        }
    }

    @GetMapping("/photo/{photoId}")
    public void getPhoto(@PathVariable int photoId, jakarta.servlet.http.HttpServletResponse resp) {
        Optional<PhotoBlob> opt = service.getPhotoById(photoId);
        if (opt.isEmpty()) {
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        PhotoBlob p = opt.get();
        try {
            resp.setContentType(p.getContentType() != null ? p.getContentType() : "application/octet-stream");
            resp.getOutputStream().write(p.getData());
        } catch (IOException e) {
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
