package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerCommentLikeRepository;
import org.wy.demo.repository.AnswerCommentRepository;
import org.wy.demo.repository.AnswerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AnswerCommentRepository answerCommentRepository;

    @Autowired
    private AnswerCommentLikeRepository answerCommentLikeRepository;

    public Answer saveAnswer(Answer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("Answer is required");
        }
        if (!StringUtils.hasText(answer.getContent())) {
            throw new IllegalArgumentException("Answer content is required");
        }
        if (answer.getQuestion() == null || answer.getQuestion().getId() == null) {
            throw new IllegalArgumentException("Question is required");
        }
        if (answer.getAuthor() == null || answer.getAuthor().getId() == null) {
            throw new IllegalArgumentException("Author is required");
        }

        Question question = questionService.getQuestionById(answer.getQuestion().getId());
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }

        User author = userService.getUserById(answer.getAuthor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        answer.setContent(answer.getContent().trim());
        answer.setQuestion(question);
        answer.setAuthor(author);
        answer.setIsAccepted(false);
        Answer savedAnswer = answerRepository.save(answer);

        if (!question.getStudent().getId().equals(author.getId())) {
            notificationService.notifyUser(
                    question.getStudent().getId(),
                    "New answer received",
                    String.format("Your question '%s' received a new answer from %s.", question.getTitle(), author.getUsername()),
                    "ANSWER_CREATED",
                    savedAnswer.getId()
            );
        }
        return savedAnswer;
    }

    public Answer getAnswerById(Integer id) {
        return answerRepository.findById(id).orElse(null);
    }

    public List<Answer> getAnswersByQuestionId(Integer questionId) {
        Question question = questionService.getQuestionById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }
        return answerRepository.findByQuestion(question);
    }

    public List<Answer> searchAnswers(String keyword, Integer questionId) {
        List<Answer> answers = StringUtils.hasText(keyword)
                ? answerRepository.findByContentContainingIgnoreCase(keyword.trim())
                : answerRepository.findAll();

        return answers.stream()
                .filter(answer -> questionId == null || (answer.getQuestion() != null && questionId.equals(answer.getQuestion().getId())))
                .sorted(Comparator.comparing(Answer::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Answer updateAnswer(Integer answerId, Answer update, Integer operatorId) {
        Answer answer = getAnswerById(answerId);
        if (answer == null) {
            throw new IllegalArgumentException("Answer not found");
        }
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        boolean isAdmin = "admin".equals(operator.getRole());
        boolean isAuthor = answer.getAuthor() != null && answer.getAuthor().getId().equals(operatorId);
        boolean isTeacher = answer.getQuestion() != null
                && answer.getQuestion().getCourse() != null
                && answer.getQuestion().getCourse().getTeacher() != null
                && answer.getQuestion().getCourse().getTeacher().getId().equals(operatorId);
        if (!isAdmin && !isAuthor && !isTeacher) {
            throw new AccessDeniedException("You do not have permission to update this answer");
        }

        if (StringUtils.hasText(update.getContent())) {
            answer.setContent(update.getContent().trim());
        }
        if (update.getIsAccepted() != null) {
            answer.setIsAccepted(update.getIsAccepted());
        }
        return answerRepository.save(answer);
    }

    public Answer acceptAnswer(Integer answerId, Integer operatorId) {
        Answer answer = getAnswerById(answerId);
        if (answer == null) {
            return null;
        }

        Question question = answer.getQuestion();
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        boolean isAdmin = "admin".equals(operator.getRole());
        if (!isAdmin
                && !question.getStudent().getId().equals(operatorId)
                && !question.getCourse().getTeacher().getId().equals(operatorId)) {
            throw new AccessDeniedException("Only the question owner, course teacher, or admin can accept an answer");
        }

        Answer oldAccepted = answerRepository.findByQuestionAndIsAccepted(question, true);
        if (oldAccepted != null && !oldAccepted.getId().equals(answer.getId())) {
            oldAccepted.setIsAccepted(false);
            answerRepository.save(oldAccepted);
        }

        answer.setIsAccepted(true);
        answerRepository.save(answer);
        questionService.markQuestionAsSolved(question.getId());

        if (!answer.getAuthor().getId().equals(operatorId)) {
            notificationService.notifyUser(
                    answer.getAuthor().getId(),
                    "Answer accepted",
                    String.format("Your answer under question '%s' was accepted by %s.", question.getTitle(), operator.getUsername()),
                    "ANSWER_ACCEPTED",
                    answer.getId()
            );
        }
        return answer;
    }

    public void deleteAnswer(Integer id, Integer operatorId) {
        Answer answer = getAnswerById(id);
        if (answer == null) {
            return;
        }
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        boolean canDelete = "admin".equals(operator.getRole())
                || answer.getAuthor().getId().equals(operatorId)
                || answer.getQuestion().getCourse().getTeacher().getId().equals(operatorId);
        if (!canDelete) {
            throw new AccessDeniedException("Only the answer author, course teacher, or admin can delete an answer");
        }

        var comments = answerCommentRepository.findByAnswerIdIn(List.of(id));
        var commentIds = comments.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (!commentIds.isEmpty()) {
            answerCommentLikeRepository.deleteByCommentIdIn(commentIds);
            answerCommentRepository.deleteAll(comments);
        }
        answerRepository.deleteById(id);
    }
}