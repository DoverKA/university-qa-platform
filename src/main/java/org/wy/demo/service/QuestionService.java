package org.wy.demo.service;

import org.wy.demo.entity.Course;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    /**
     * 学生提交提问（高校场景：仅学生可提问，必须关联课程）
     */
    public String submitQuestion(Question question) {
        // 1. 基础参数校验
        if (!StringUtils.hasText(question.getTitle())) {
            return "问题标题不能为空";
        }
        if (!StringUtils.hasText(question.getContent())) {
            return "问题详情不能为空";
        }
        if (question.getCourse() == null || question.getCourse().getId() == null) {
            return "所属课程不能为空";
        }
        if (question.getStudent() == null || question.getStudent().getId() == null) {
            return "提问学生不能为空";
        }

        // 2. 高校权限校验：仅学生可提问
        User student = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(question.getStudent().getId()))
                .findFirst()
                .orElse(null);
        if (student == null) {
            return "学生不存在";
        }
        if (!"student".equals(student.getRole())) {
            return "仅学生账号可提交提问";
        }

        // 3. 校验课程是否存在
        Course course = courseService.getCourseById(question.getCourse().getId());
        if (course == null) {
            return "所属课程不存在";
        }

        // 4. 保存问题
        questionRepository.save(question);
        return "提问提交成功";
    }

    /**
     * 按课程ID查询问题（核心：学生看本课程的问题）
     */
    public List<Question> getQuestionsByCourseId(Integer courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    /**
     * 按课程ID+解决状态查询（筛选未解决/已解决）
     */
    public List<Question> getQuestionsByCourseIdAndIsSolved(Integer courseId, Boolean isSolved) {
        return questionRepository.findByCourseIdAndIsSolved(courseId, isSolved);
    }

    /**
     * 按学生ID查询自己提的问题
     */
    public List<Question> getQuestionsByStudentId(Integer studentId) {
        return questionRepository.findByStudentId(studentId);
    }

    /**
     * 标记问题已解决（仅提问学生/教师可操作）
     */
    public String markQuestionSolved(Integer questionId, Integer operatorId) {
        // 1. 查询问题
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return "问题不存在";
        }

        // 2. 校验操作权限：提问学生 或 课程教师
        User operator = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(operatorId))
                .findFirst()
                .orElse(null);
        if (operator == null) {
            return "操作人不存在";
        }

        // 2.1 学生：只能标记自己的问题
        if ("student".equals(operator.getRole())
                && !question.getStudent().getId().equals(operatorId)) {
            return "仅可标记自己的问题为已解决";
        }

        // 2.2 教师：只能标记自己课程的问题
        if ("teacher".equals(operator.getRole())
                && !question.getCourse().getTeacher().getId().equals(operatorId)) {
            return "仅可标记自己课程的问题为已解决";
        }

        // 3. 标记为已解决
        question.setIsSolved(true);
        questionRepository.save(question);
        return "问题已标记为已解决";
    }

    /**
     * 查询所有问题（管理员用）
     */
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    /**
     * 按ID查询单条问题
     */
    public Question getQuestionById(Integer id) {
        return questionRepository.findById(id).orElse(null);
    }

    public void markQuestionAsSolved(Integer id) {
    }

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
}