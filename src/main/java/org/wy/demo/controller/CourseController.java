package org.wy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.User;
import org.wy.demo.security.SecurityUtils;
import org.wy.demo.service.CourseService;
import org.wy.demo.service.SqlSyncService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private SqlSyncService sqlSyncService;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PostMapping
    public String createCourse(@RequestBody Course course) {
        if (!"teacher".equals(SecurityUtils.getCurrentRole())) {
            throw new AccessDeniedException("Only teachers can create courses");
        }
        if (course.getTeacher() == null) {
            course.setTeacher(new User());
        }
        course.getTeacher().setId(SecurityUtils.getCurrentUserId());
        return courseService.saveCourse(course);
    }

    @PostMapping("/sync/sql")
    public Map<String, Integer> syncCoursesFromSql() {
        return sqlSyncService.syncTeachersAndCourses();
    }
}
