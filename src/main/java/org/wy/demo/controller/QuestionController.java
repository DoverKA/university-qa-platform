package org.wy.demo.controller;

import org.springframework.stereotype.Controller;
import org.wy.demo.entity.Question;
import org.wy.demo.service.QuestionService;
import org.wy.demo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RestController
@RequestMapping("/api/questions")
@CrossOrigin // 允许跨域，前端调用不报错
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiService aiService;

    /**
     * 学生提交提问（POST请求）
     * 示例请求体：
     * {
     *   "title": "Spring Boot怎么配置JPA？",
     *   "content": "我在配置application.properties时，数据库连接总是报错...",
     *   "course": {"id": 1}, // 课程ID
     *   "student": {"id": 2} // 学生ID
     * }
     */
    @PostMapping
    public String submitQuestion(@RequestBody Question question) {
        return questionService.submitQuestion(question);
    }

    /**
     * 按课程ID查询问题（GET请求）
     * URL：http://localhost:8080/question/course/1
     */
    @GetMapping("/course/{courseId}")
    public List<Question> getQuestionsByCourseId(@PathVariable Integer courseId) {
        return questionService.getQuestionsByCourseId(courseId);
    }

    /**
     * 按课程ID+解决状态查询（GET请求）
     * URL：http://localhost:8080/question/course/1/solved/false
     */
    @GetMapping("/course/{courseId}/solved/{isSolved}")
    public List<Question> getQuestionsByCourseIdAndIsSolved(
            @PathVariable Integer courseId,
            @PathVariable Boolean isSolved) {
        return questionService.getQuestionsByCourseIdAndIsSolved(courseId, isSolved);
    }

    /**
     * 按学生ID查询自己的问题（GET请求）
     * URL：http://localhost:8080/question/student/2
     */
    @GetMapping("/student/{studentId}")
    public List<Question> getQuestionsByStudentId(@PathVariable Integer studentId) {
        return questionService.getQuestionsByStudentId(studentId);
    }

    /**
     * 标记问题已解决（PUT请求）
     * URL：http://localhost:8080/question/1/solve?operatorId=2
     */
    @PutMapping("/{questionId}/solve")
    public String markQuestionSolved(
            @PathVariable Integer questionId,
            @RequestParam Integer operatorId) {
        return questionService.markQuestionSolved(questionId, operatorId);
    }

    /**
     * 查询所有问题（GET请求）
     * URL：http://localhost:8080/question
     */
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    /**
     * 按ID查询单条问题（GET请求）
     * URL：http://localhost:8080/question/1
     */
    @GetMapping("/{id}")
    public Question getQuestionById(@PathVariable Integer id) {
        return questionService.getQuestionById(id);
    }

    @PutMapping("/{questionId}/ai-answer")
    public Question generateAiAnswer(@PathVariable Integer questionId) {
        Question question = questionService.getQuestionById(questionId);
        if (question == null) {
            return null;
        }
        String aiAnswer = aiService.generateAnswer(
            question.getTitle(),
            question.getContent(),
            question.getCourse().getName()
        );
        question.setAiAnswer(aiAnswer);
        return questionService.saveQuestion(question);
    }
}