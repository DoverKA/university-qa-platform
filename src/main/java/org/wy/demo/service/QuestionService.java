package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerRepository;
import org.wy.demo.repository.QuestionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AnswerRepository answerRepository;

    public String submitQuestion(Question question) {
        if (question == null) {
            return "Question is required";
        }
        if (!StringUtils.hasText(question.getTitle())) {
            return "Question title is required";
        }
        if (!StringUtils.hasText(question.getContent())) {
            return "Question content is required";
        }
        if (question.getCourse() == null || question.getCourse().getId() == null) {
            return "Course is required";
        }
        if (question.getStudent() == null || question.getStudent().getId() == null) {
            return "Student is required";
        }

        User student = userService.getUserById(question.getStudent().getId()).orElse(null);
        if (student == null) {
            return "Student not found";
        }
        if (!"student".equals(student.getRole())) {
            return "Only students can submit questions";
        }

        Course course = courseService.getCourseById(question.getCourse().getId());
        if (course == null) {
            return "Course not found";
        }

        question.setTitle(question.getTitle().trim());
        question.setContent(question.getContent().trim());
        question.setStudent(student);
        question.setCourse(course);
        question.setIsSolved(false);
        Question savedQuestion = questionRepository.save(question);

        if (course.getTeacher() != null && course.getTeacher().getId() != null) {
            notificationService.notifyUser(
                    course.getTeacher().getId(),
                    "收到新的课程问题",
                    String.format("课程《%s》收到了来自 %s 的新问题：%s", course.getName(), student.getUsername(), savedQuestion.getTitle()),
                    "QUESTION_CREATED",
                    savedQuestion.getId()
            );
        }
        return "Question submitted successfully";
    }

    public List<Question> getQuestionsByCourseId(Integer courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    public List<Question> getQuestionsByCourseIdAndIsSolved(Integer courseId, Boolean isSolved) {
        return questionRepository.findByCourseIdAndIsSolved(courseId, isSolved);
    }

    public List<Question> getQuestionsByStudentId(Integer studentId) {
        return questionRepository.findByStudentId(studentId);
    }

    public String markQuestionSolved(Integer questionId, Integer operatorId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return "Question not found";
        }

        User operator = userService.getUserById(operatorId).orElse(null);
        if (operator == null) {
            return "Operator not found";
        }

        boolean admin = "admin".equals(operator.getRole());
        if ("student".equals(operator.getRole())
                && !question.getStudent().getId().equals(operatorId)) {
            return "Students can only solve their own questions";
        }

        if ("teacher".equals(operator.getRole())
                && !question.getCourse().getTeacher().getId().equals(operatorId)) {
            return "Teachers can only solve questions in their own courses";
        }

        if (!admin && !"student".equals(operator.getRole()) && !"teacher".equals(operator.getRole())) {
            return "Current role is not allowed to solve questions";
        }

        question.setIsSolved(true);
        questionRepository.save(question);

        if (!question.getStudent().getId().equals(operatorId)) {
            notificationService.notifyUser(
                    question.getStudent().getId(),
                    "你的问题已被标记为解决",
                    String.format("问题《%s》已被 %s 标记为已解决。", question.getTitle(), operator.getUsername()),
                    "QUESTION_SOLVED",
                    question.getId()
            );
        }
        return "Question marked as solved";
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getVisibleQuestions(Integer viewerId, String viewerRole) {
        return filterQuestionsForViewer(questionRepository.findAll(), viewerId, viewerRole);
    }

    public List<Question> searchQuestions(String keyword, Integer courseId, Boolean solved) {
        List<Question> questions = StringUtils.hasText(keyword)
                ? questionRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword.trim(), keyword.trim())
                : questionRepository.findAll();

        return questions.stream()
                .filter(question -> courseId == null || (question.getCourse() != null && courseId.equals(question.getCourse().getId())))
                .filter(question -> solved == null || solved.equals(question.getIsSolved()))
                .sorted(Comparator.comparing(Question::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Question> searchQuestionsForViewer(Integer viewerId, String viewerRole, String keyword, Integer courseId, Boolean solved) {
        return filterQuestionsForViewer(searchQuestions(keyword, courseId, solved), viewerId, viewerRole);
    }

    public Question getQuestionById(Integer id) {
        return questionRepository.findById(id).orElse(null);
    }

    public Question updateQuestion(Integer questionId, Question update, Integer operatorId) {
        Question existing = getQuestionById(questionId);
        if (existing == null) {
            throw new IllegalArgumentException("Question not found");
        }
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        boolean isAdmin = "admin".equals(operator.getRole());
        boolean isOwner = existing.getStudent() != null && existing.getStudent().getId().equals(operatorId);
        boolean isTeacher = existing.getCourse() != null
                && existing.getCourse().getTeacher() != null
                && existing.getCourse().getTeacher().getId().equals(operatorId);
        if (!isAdmin && !isOwner && !isTeacher) {
            throw new AccessDeniedException("You do not have permission to update this question");
        }

        if (StringUtils.hasText(update.getTitle())) {
            existing.setTitle(update.getTitle().trim());
        }
        if (StringUtils.hasText(update.getContent())) {
            existing.setContent(update.getContent().trim());
        }
        if (update.getCourse() != null && update.getCourse().getId() != null) {
            Course course = courseService.getCourseById(update.getCourse().getId());
            if (course == null) {
                throw new IllegalArgumentException("Course not found");
            }
            existing.setCourse(course);
        }
        if (update.getIsSolved() != null) {
            existing.setIsSolved(update.getIsSolved());
        }
        if (update.getAiAnswer() != null) {
            existing.setAiAnswer(update.getAiAnswer().trim());
        }
        return questionRepository.save(existing);
    }

    public void deleteQuestion(Integer questionId, Integer operatorId) {
        Question existing = getQuestionById(questionId);
        if (existing == null) {
            return;
        }
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        boolean isAdmin = "admin".equals(operator.getRole());
        boolean isOwner = existing.getStudent() != null && existing.getStudent().getId().equals(operatorId);
        boolean isTeacher = existing.getCourse() != null
                && existing.getCourse().getTeacher() != null
                && existing.getCourse().getTeacher().getId().equals(operatorId);
        if (!isAdmin && !isOwner && !isTeacher) {
            throw new AccessDeniedException("You do not have permission to delete this question");
        }
        answerRepository.deleteAll(answerRepository.findByQuestion(existing));
        questionRepository.deleteById(questionId);
    }

    public void markQuestionAsSolved(Integer id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question != null) {
            question.setIsSolved(true);
            questionRepository.save(question);
        }
    }

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    public Map<String, Long> getQuestionStats() {
        return Map.of(
                "totalQuestions", questionRepository.count(),
                "solvedQuestions", questionRepository.countByIsSolved(true),
                "unsolvedQuestions", questionRepository.countByIsSolved(false)
        );
    }

    private List<Question> filterQuestionsForViewer(List<Question> questions, Integer viewerId, String viewerRole) {
        if ("admin".equals(viewerRole) || "student".equals(viewerRole)) {
            return questions.stream()
                    .sorted(Comparator.comparing(Question::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        if ("teacher".equals(viewerRole)) {
            return questions.stream()
                    .filter(question -> question.getCourse() != null
                            && question.getCourse().getTeacher() != null
                            && viewerId != null
                            && viewerId.equals(question.getCourse().getTeacher().getId()))
                    .sorted(Comparator.comparing(Question::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
