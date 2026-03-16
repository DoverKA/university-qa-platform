package org.wy.demo.controller;

import org.springframework.stereotype.Controller;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Question;
import org.wy.demo.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    @Autowired
    private AnswerService answerService;
    // 提交回答
    @PostMapping
    public ResponseEntity<Answer> createAnswer(@RequestBody Answer answer) {
        Answer savedAnswer = answerService.saveAnswer(answer);
        return new ResponseEntity<>(savedAnswer, HttpStatus.CREATED);
    }
    // 根据问题ID查询所有回答
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Answer>> getAnswersByQuestion(@PathVariable Integer questionId) {
        // 简化示例：实际需注入QuestionService获取Question对象
        Question question = new Question();
        question.setId(questionId);
        List<Answer> answers = answerService.getAnswersByQuestion(question);
        return ResponseEntity.ok(answers);
    }
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Answer>> getAnswersByAuthor(@PathVariable Integer authorId) {
        return ResponseEntity.ok(answerService.getAnswersByAuthorId(authorId));
    }
    
    // 采纳最佳答案
    @PutMapping("/{id}/accept")
    public ResponseEntity<Answer> acceptAnswer(@PathVariable Integer id) {
        Answer answer = answerService.acceptAnswer(id);
        return answer != null ? ResponseEntity.ok(answer) : ResponseEntity.notFound().build();
    }
    // 删除回答
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }
}