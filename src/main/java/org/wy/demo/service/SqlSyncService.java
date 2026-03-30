package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wy.demo.config.SqlSyncProperties;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.User;
import org.wy.demo.repository.CourseRepository;
import org.wy.demo.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SqlSyncService {

    @Autowired
    private SqlSyncProperties sqlSyncProperties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Integer> syncTeachersAndCourses() {
        if (!sqlSyncProperties.isEnabled()) {
            throw new IllegalStateException("SQL sync is disabled. Set sync.sql.enabled=true first.");
        }

        int teachersCreated = 0;
        int coursesCreated = 0;
        int coursesSkipped = 0;

        List<Map<String, Object>> teacherRows = jdbcTemplate.queryForList(sqlSyncProperties.getTeacherQuery());
        for (Map<String, Object> row : teacherRows) {
            String username = asText(row.get("username"));
            if (!StringUtils.hasText(username) || userRepository.existsByUsername(username)) {
                continue;
            }

            User teacher = new User();
            teacher.setUsername(username);
            teacher.setEmail(asText(row.get("email")));
            teacher.setRole("teacher");
            teacher.setPassword(passwordEncoder.encode(sqlSyncProperties.getDefaultTeacherPassword()));
            userRepository.save(teacher);
            teachersCreated++;
        }

        List<Map<String, Object>> courseRows = jdbcTemplate.queryForList(sqlSyncProperties.getCourseQuery());
        for (Map<String, Object> row : courseRows) {
            String name = asText(row.get("name"));
            String major = asText(row.get("major"));
            String semester = asText(row.get("semester"));
            String teacherUsername = asText(row.get("teacher_username"));

            if (!StringUtils.hasText(name)
                    || !StringUtils.hasText(major)
                    || !StringUtils.hasText(semester)
                    || !StringUtils.hasText(teacherUsername)) {
                coursesSkipped++;
                continue;
            }

            Optional<User> teacherOpt = userRepository.findByUsername(teacherUsername);
            if (teacherOpt.isEmpty()) {
                coursesSkipped++;
                continue;
            }

            User teacher = teacherOpt.get();
            if (!"teacher".equals(teacher.getRole())) {
                coursesSkipped++;
                continue;
            }

            boolean exists = courseRepository.existsByNameAndMajorAndSemesterAndTeacherId(
                    name, major, semester, teacher.getId());
            if (exists) {
                coursesSkipped++;
                continue;
            }

            Course course = new Course();
            course.setName(name);
            course.setMajor(major);
            course.setSemester(semester);
            course.setDescription(asText(row.get("description")));
            course.setTeacher(teacher);
            courseRepository.save(course);
            coursesCreated++;
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("teachersCreated", teachersCreated);
        result.put("coursesCreated", coursesCreated);
        result.put("coursesSkipped", coursesSkipped);
        return result;
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
