package org.wy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wy.demo.dto.AnswerCommentRequest;
import org.wy.demo.dto.AnswerCommentResponse;
import org.wy.demo.security.SecurityUtils;
import org.wy.demo.service.AnswerCommentService;
import org.wy.demo.service.AnswerService;

import java.util.List;

import org.wy.demo.entity.Answer;
import org.wy.demo.entity.User;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerCommentService answerCommentService;

    @PostMapping
    public ResponseEntity<Answer> createAnswer(@RequestBody Answer answer) {
        if (answer.getAuthor() == null) {
            answer.setAuthor(new User());
        }
        answer.getAuthor().setId(SecurityUtils.getCurrentUserId());
        Answer savedAnswer = answerService.saveAnswer(answer);
        return new ResponseEntity<>(savedAnswer, HttpStatus.CREATED);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Answer>> getAnswersByQuestion(@PathVariable Integer questionId) {
        List<Answer> answers = answerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/{answerId}/comments")
    public ResponseEntity<List<AnswerCommentResponse>> getCommentsByAnswer(@PathVariable Integer answerId) {
        return ResponseEntity.ok(answerCommentService.getCommentsByAnswerId(answerId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{answerId}/comments")
    public ResponseEntity<AnswerCommentResponse> createComment(@PathVariable Integer answerId,
                                                               @RequestBody AnswerCommentRequest request) {
        return new ResponseEntity<>(answerCommentService.createComment(answerId, SecurityUtils.getCurrentUserId(), request), HttpStatus.CREATED);
    }

    @PutMapping("/comments/{commentId}/like")
    public ResponseEntity<AnswerCommentResponse> toggleCommentLike(@PathVariable Integer commentId) {
        return ResponseEntity.ok(answerCommentService.toggleLike(commentId, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Answer> acceptAnswer(@PathVariable Integer id) {
        Answer answer = answerService.acceptAnswer(id, SecurityUtils.getCurrentUserId());
        return answer != null ? ResponseEntity.ok(answer) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer id) {
        answerService.deleteAnswer(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
