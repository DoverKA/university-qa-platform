package org.wy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.security.SecurityUtils;
import org.wy.demo.service.AnswerService;
import org.wy.demo.service.CourseService;
import org.wy.demo.service.QuestionService;
import org.wy.demo.service.SqlSyncService;
import org.wy.demo.service.StatsService;
import org.wy.demo.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manage")
public class ManagementController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private SqlSyncService sqlSyncService;

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return statsService.getOverviewStats(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/stats/course-distribution")
    public Map<String, Long> getCourseDistribution() {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return statsService.getCourseQuestionDistribution();
    }

    @GetMapping("/courses")
    public List<Course> manageCourses(@RequestParam(required = false) String keyword) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return courseService.searchCourses(keyword);
    }

    @PostMapping("/courses")
    public String createManagedCourse(@RequestBody Course course) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        Integer operatorId = SecurityUtils.getCurrentUserId();
        if (course.getTeacher() == null) {
            course.setTeacher(new User());
        }
        if (!SecurityUtils.hasRole("admin")) {
            course.getTeacher().setId(operatorId);
        }
        return courseService.saveCourse(course, operatorId);
    }

    @PutMapping("/courses/{id}")
    public String updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        course.setId(id);
        if (course.getTeacher() == null) {
            course.setTeacher(new User());
        }
        if (!SecurityUtils.hasRole("admin")) {
            course.getTeacher().setId(SecurityUtils.getCurrentUserId());
        }
        return courseService.saveCourse(course, SecurityUtils.getCurrentUserId());
    }

    @DeleteMapping("/courses/{id}")
    public Map<String, String> deleteCourse(@PathVariable Integer id) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        courseService.deleteCourse(id, SecurityUtils.getCurrentUserId());
        return Map.of("message", "Course deleted");
    }

    @GetMapping("/questions")
    public List<Question> manageQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Boolean solved) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return questionService.searchQuestionsForViewer(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentRole(),
                keyword,
                courseId,
                solved
        );
    }

    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable Integer id, @RequestBody Question question) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return questionService.updateQuestion(id, question, SecurityUtils.getCurrentUserId());
    }

    @DeleteMapping("/questions/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Integer id) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        questionService.deleteQuestion(id, SecurityUtils.getCurrentUserId());
        return Map.of("message", "Question deleted");
    }

    @GetMapping("/search/questions")
    public List<Question> searchQuestions(@RequestParam String keyword) {
        return questionService.searchQuestions(keyword, null, null);
    }

    @GetMapping("/answers")
    public List<Answer> manageAnswers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer questionId) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return answerService.searchAnswers(keyword, questionId);
    }

    @PutMapping("/answers/{id}")
    public Answer updateAnswer(@PathVariable Integer id, @RequestBody Answer answer) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        return answerService.updateAnswer(id, answer, SecurityUtils.getCurrentUserId());
    }

    @DeleteMapping("/answers/{id}")
    public Map<String, String> deleteManagedAnswer(@PathVariable Integer id) {
        SecurityUtils.requireAnyRole("teacher", "admin");
        answerService.deleteAnswer(id, SecurityUtils.getCurrentUserId());
        return Map.of("message", "Answer deleted");
    }

    @GetMapping("/system/users")
    public List<User> getAllUsers() {
        SecurityUtils.requireAnyRole("admin");
        return userService.getAllUsers();
    }

    @PutMapping("/system/users/{id}/role")
    public User updateUserRole(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        SecurityUtils.requireAnyRole("admin");
        return userService.updateRole(id, payload.get("role"), SecurityUtils.getCurrentUserId());
    }

    @DeleteMapping("/system/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Integer id) {
        SecurityUtils.requireAnyRole("admin");
        userService.deleteUser(id, SecurityUtils.getCurrentUserId());
        return Map.of("message", "User deleted");
    }

    @PostMapping("/system/sql-sync")
    public Map<String, Integer> syncSql() {
        SecurityUtils.requireAnyRole("admin");
        return sqlSyncService.syncTeachersAndCourses();
    }
}
