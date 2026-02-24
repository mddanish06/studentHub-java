package com.studentapp.student_management.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

@Service
public class StudentService {

    @Autowired
    private JdbcTemplate jdbc;

    public List<Map<String, Object>> listStudents(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT m.id, m.name, m.age, m.dob, m.address, m.state_id, s.name AS state, m.phone " +
                "FROM student_master m LEFT JOIN state_name s ON m.state_id = s.id " +
                "ORDER BY m.id DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, size, offset);

        for (Map<String, Object> r : rows) {
            Integer sid = (Integer) r.get("id");
            List<String> subjects = jdbc.queryForList(
                    "SELECT subject_name FROM student_detail WHERE student_id = ?",
                    new Object[] { sid },
                    String.class);
            r.put("subjects", subjects);

            List<Map<String, Object>> photos = jdbc.query(
                    "SELECT id, file_name FROM student_photo WHERE student_id = ?",
                    new Object[] { sid },
                    (rs, rowNum) -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("file_name", rs.getString("file_name"));
                        return m;
                    });
            r.put("photos", photos);
        }

        return rows;
    }

    public int getTotalStudentsCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM student_master", Integer.class);
    }

    public Map<String, Object> getStudentById(int id) {
        Map<String, Object> master = jdbc.queryForMap("SELECT * FROM student_master WHERE id = ?", id);

        List<Map<String, Object>> subjects = jdbc.query(
                "SELECT id, subject_name FROM student_detail WHERE student_id = ?",
                new Object[] { id },
                (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getInt("id"));
                    m.put("subject_name", rs.getString("subject_name"));
                    return m;
                });

        List<Map<String, Object>> photos = jdbc.query(
                "SELECT id, file_name FROM student_photo WHERE student_id = ?",
                new Object[] { id },
                (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getInt("id"));
                    m.put("file_name", rs.getString("file_name"));
                    return m;
                });

        Map<String, Object> result = new HashMap<>();
        result.put("master", master);
        result.put("subjects", subjects);
        result.put("photos", photos);
        return result;
    }

    @Transactional
    public int createStudent(String name, int age, java.util.Date dob, String address, Integer stateId, String phone,
            List<String> subjects, List<MultipartFile> photos) throws IOException {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO student_master (name, age, dob, address, state_id, phone) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setDate(3, (Date) dob);
            ps.setString(4, address);
            if (stateId != null)
                ps.setInt(5, stateId);
            else
                ps.setNull(5, Types.INTEGER);
            ps.setString(6, phone);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        int studentId = (key != null) ? key.intValue() : -1;

        if (subjects != null && !subjects.isEmpty()) {
            for (String subj : subjects) {
                if (subj == null)
                    continue;
                jdbc.update("INSERT INTO student_detail (student_id, subject_name) VALUES (?, ?)", studentId, subj);
            }
        }

        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile mf : photos) {
                if (mf == null || mf.isEmpty())
                    continue;
                byte[] bytes = mf.getBytes();
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO student_photo (student_id, file_name, data, content_type) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, studentId);
                    ps.setString(2, mf.getOriginalFilename());
                    ps.setBytes(3, bytes);
                    ps.setString(4, mf.getContentType());
                    return ps;
                });
            }
        }

        return studentId;
    }

    @Transactional
    public void updateStudent(int id, String name, int age, Date dob, String address, Integer stateId, String phone,
            List<String> subjects, List<MultipartFile> photos) throws IOException {

        jdbc.update("UPDATE student_master SET name=?, age=?, dob=?, address=?, state_id=?, phone=? WHERE id=?",
                name, age, dob, address, stateId, phone, id);

        jdbc.update("DELETE FROM student_detail WHERE student_id = ?", id);
        if (subjects != null && !subjects.isEmpty()) {
            for (String subj : subjects) {
                jdbc.update("INSERT INTO student_detail (student_id, subject_name) VALUES (?, ?)", id, subj);
            }
        }

        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile mf : photos) {
                if (mf == null || mf.isEmpty())
                    continue;
                byte[] bytes = mf.getBytes();
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO student_photo (student_id, file_name, data, content_type) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, id);
                    ps.setString(2, mf.getOriginalFilename());
                    ps.setBytes(3, bytes);
                    ps.setString(4, mf.getContentType());
                    return ps;
                });
            }
        }
    }

    @Transactional
    public void deleteStudent(int id) {

        jdbc.update("DELETE FROM student_master WHERE id = ?", id);
    }

    public Optional<PhotoBlob> getPhotoById(int photoId) {
        List<PhotoBlob> list = jdbc.query(
                "SELECT id, file_name, data, content_type FROM student_photo WHERE id = ?",
                new Object[] { photoId },
                (rs, rowNum) -> {
                    PhotoBlob p = new PhotoBlob();
                    p.setId(rs.getInt("id"));
                    p.setFileName(rs.getString("file_name"));
                    p.setData(rs.getBytes("data"));
                    p.setContentType(rs.getString("content_type"));
                    return p;
                });
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list.get(0));
    }

    public static class PhotoBlob {
        private int id;
        private String fileName;
        private byte[] data;
        private String contentType;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
}
