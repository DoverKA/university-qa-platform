package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.Question;
import org.wy.demo.repository.AnswerRepository;
import org.wy.demo.repository.CourseRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private NotificationService notificationService;

    public Map<String, Object> getOverviewStats(Integer userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(userService.getUserStats());
        result.putAll(questionService.getQuestionStats());
        result.put("totalCourses", courseRepository.count());
        result.put("totalAnswers", answerRepository.count());
        result.put("unreadNotifications", notificationService.getUnreadCount(userId));
        result.put("recentQuestions", questionService.getAllQuestions().stream()
                .sorted(Comparator.comparing(Question::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList()));
        result.put("recentCourses", courseRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList()));
        result.put("recentAnswers", answerRepository.findAll().stream()
                .sorted(Comparator.comparing(Answer::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList()));
        return result;
    }

    public Map<String, Long> getCourseQuestionDistribution() {
        return questionService.getAllQuestions().stream()
                .filter(question -> question.getCourse() != null)
                .collect(Collectors.groupingBy(question -> question.getCourse().getName(), LinkedHashMap::new, Collectors.counting()));
    }
}
