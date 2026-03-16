package org.wy.demo.controller;

import org.springframework.stereotype.Controller;
import org.wy.demo.entity.Course;
import org.wy.demo.service.CourseService;
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

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Course> getCoursesByTeacherId(@PathVariable Integer teacherId) {
        return courseService.getCoursesByTeacherId(teacherId);
    }

    @PostMapping
    public String createCourse(@RequestBody Course course) {
        return courseService.saveCourse(course);
    }
}