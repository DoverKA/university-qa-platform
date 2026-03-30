package org.wy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.security.SecurityUtils;
import org.wy.demo.service.AiService;
import org.wy.demo.service.QuestionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiService aiService;

    @PostMapping
    public String submitQuestion(@RequestBody Question question) {
        if (!"student".equals(SecurityUtils.getCurrentRole())) {
            throw new AccessDeniedException("Only students can submit questions");
        }
        if (question.getStudent() == null) {
            question.setStudent(new User());
        }
        question.getStudent().setId(SecurityUtils.getCurrentUserId());
        return questionService.submitQuestion(question);
    }

    @GetMapping("/course/{courseId}")
    public List<Question> getQuestionsByCourseId(@PathVariable Integer courseId) {
        return questionService.getQuestionsByCourseId(courseId);
    }

    @GetMapping("/course/{courseId}/solved/{isSolved}")
    public List<Question> getQuestionsByCourseIdAndIsSolved(
            @PathVariable Integer courseId,
            @PathVariable Boolean isSolved) {
        return questionService.getQuestionsByCourseIdAndIsSolved(courseId, isSolved);
    }

    @GetMapping("/student/{studentId}")
    public List<Question> getQuestionsByStudentId(@PathVariable Integer studentId) {
        return questionService.getQuestionsByStudentId(studentId);
    }

    @PutMapping("/{questionId}/solve")
    public String markQuestionSolved(@PathVariable Integer questionId) {
        return questionService.markQuestionSolved(questionId, SecurityUtils.getCurrentUserId());
    }

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getVisibleQuestions(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentRole()
        );
    }

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

    @PostMapping("/preview-ai")
    public String previewAiAnswer(@RequestBody Map<String, String> request) {
        String courseName = request.get("courseName");
        String title = request.get("title");
        String content = request.get("content");

        if (!StringUtils.hasText(courseName)) {
            throw new IllegalArgumentException("Course name is required");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Question title is required");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Question content is required");
        }
        return aiService.generateAnswer(
                title.trim(),
                content.trim(),
                courseName.trim()
        );
    }
}
