package org.wy.demo.controller;

import org.springframework.stereotype.Controller;
import org.wy.demo.dto.SqlSyncResult;
import org.wy.demo.entity.Course;
import org.wy.demo.service.CourseService;
import org.wy.demo.service.SqlSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
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
        return courseService.saveCourse(course);
    }

    @PostMapping("/sync/sql")
    public SqlSyncResult syncCoursesFromSql() {
        return sqlSyncService.syncTeachersAndCourses();
    }
}
