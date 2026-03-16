package org.wy.demo.service;

import lombok.Data;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.User;
import org.wy.demo.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
@Data
@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService; // 依赖你已实现的UserService

    // 重写：新增/修改课程（加高校场景权限校验）
    public String saveCourse(Course course) {
        // 1. 基础参数校验（贴合高校场景）
        if (!StringUtils.hasText(course.getName())) {
            return "课程名称不能为空";
        }
        if (!StringUtils.hasText(course.getMajor())) {
            return "所属专业不能为空";
        }
        if (!StringUtils.hasText(course.getSemester())) {
            return "开课学期不能为空";
        }
        if (course.getTeacher() == null || course.getTeacher().getId() == null) {
            return "授课教师不能为空";
        }

        // 2. 校验教师身份（仅教师可创建/修改课程）
        // 从UserService获取教师信息（你原有UserService已实现查询用户）
        User teacher = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(course.getTeacher().getId()))
                .findFirst()
                .orElse(null);

        if (teacher == null) {
            return "教师不存在";
        }
        if (!"teacher".equals(teacher.getRole())) {
            return "仅教师账号可创建/修改课程";
        }

        // 3. 保存课程（保留你原有逻辑）
        courseRepository.save(course);
        return course.getId() == null ? "课程创建成功" : "课程修改成功";
    }

    // 保留你原有方法：根据ID查询课程
    public Course getCourseById(Integer id) {
        return courseRepository.findById(id).orElse(null);
    }

    // 保留你原有方法：根据教师对象查询课程
    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    // 新增：根据教师ID查询课程（更实用）
    public List<Course> getCoursesByTeacherId(Integer teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    // 保留你原有方法：模糊查询课程
    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByNameContaining(keyword);
    }

    // 保留你原有方法：删除课程
    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    // 保留你原有方法：查询所有课程
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // 保留你原有方法：按专业+学期查询课程
    public List<Course> getCoursesByMajorAndSemester(String major, String semester) {
        return courseRepository.findByMajorAndSemester(major, semester);
    }


    // 保留你原有方法：按专业查询课程
    public List<Course> getCoursesByMajor(String major) {
        return courseRepository.findByMajor(major);
    }
}