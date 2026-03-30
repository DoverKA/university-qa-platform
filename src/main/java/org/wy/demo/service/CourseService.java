package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerRepository;
import org.wy.demo.repository.CourseRepository;
import org.wy.demo.repository.QuestionRepository;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public String saveCourse(Course course) {
        return saveCourse(course, course != null && course.getTeacher() != null ? course.getTeacher().getId() : null);
    }

    public String saveCourse(Course course, Integer operatorId) {
        if (course == null) {
            return "Course is required";
        }
        if (!StringUtils.hasText(course.getName())) {
            return "Course name is required";
        }
        if (!StringUtils.hasText(course.getMajor())) {
            return "Course major is required";
        }
        if (!StringUtils.hasText(course.getSemester())) {
            return "Course semester is required";
        }
        if (course.getTeacher() == null || course.getTeacher().getId() == null) {
            return "Teacher is required";
        }

        User teacher = userService.getUserById(course.getTeacher().getId()).orElse(null);
        if (teacher == null) {
            return "Teacher not found";
        }
        if (!"teacher".equals(teacher.getRole())) {
            return "Only teachers can create or update courses";
        }

        User operator = operatorId != null ? userService.getUserById(operatorId).orElse(null) : teacher;
        if (operator == null) {
            return "Operator not found";
        }
        boolean admin = "admin".equals(operator.getRole());
        if (!admin && !teacher.getId().equals(operator.getId())) {
            throw new AccessDeniedException("You can only manage your own courses");
        }

        if (course.getId() == null
                && courseRepository.existsByNameAndMajorAndSemesterAndTeacherId(
                course.getName().trim(),
                course.getMajor().trim(),
                course.getSemester().trim(),
                teacher.getId())) {
            return "You already created this course";
        }

        Integer originalId = course.getId();
        course.setName(course.getName().trim());
        course.setMajor(course.getMajor().trim());
        course.setSemester(course.getSemester().trim());
        if (course.getDescription() != null) {
            course.setDescription(course.getDescription().trim());
        }
        course.setTeacher(teacher);
        courseRepository.save(course);
        return originalId == null ? "Course created successfully" : "Course updated successfully";
    }

    public Course getCourseById(Integer id) {
        return courseRepository.findById(id).orElse(null);
    }

    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    public List<Course> getCoursesByTeacherId(Integer teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public List<Course> searchCourses(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return courseRepository.findAll();
        }
        String normalized = keyword.trim();
        return courseRepository.findByNameContainingIgnoreCaseOrMajorContainingIgnoreCaseOrSemesterContainingIgnoreCase(
                normalized,
                normalized,
                normalized
        );
    }

    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    public void deleteCourse(Integer id, Integer operatorId) {
        Course course = getCourseById(id);
        if (course == null) {
            return;
        }
        User operator = userService.getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        if (!"admin".equals(operator.getRole()) && !course.getTeacher().getId().equals(operatorId)) {
            throw new AccessDeniedException("You can only delete your own courses");
        }
        List<Question> questions = questionRepository.findByCourseId(id);
        questions.forEach(question -> {
            answerRepository.deleteAll(answerRepository.findByQuestion(question));
            questionRepository.deleteById(question.getId());
        });
        courseRepository.deleteById(id);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByMajorAndSemester(String major, String semester) {
        return courseRepository.findByMajorAndSemester(major, semester);
    }

    public List<Course> getCoursesByMajor(String major) {
        return courseRepository.findByMajor(major);
    }
}
