package org.wy.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.Notification;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerRepository;
import org.wy.demo.repository.CourseRepository;
import org.wy.demo.repository.NotificationRepository;
import org.wy.demo.repository.QuestionRepository;
import org.wy.demo.repository.UserRepository;

@Component
@Profile("!test")
public class DemoDataInitializer implements CommandLineRunner {

    @Value("${app.demo-data.enabled:true}")
    private boolean enabled;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.count() > 0) {
            return;
        }

        User admin = createUser("admin", "123456", "admin@uniask.local", "admin");
        User teacher = createUser("teacher_liu", "123456", "liu@uniask.local", "teacher");
        User student = createUser("student_lin", "123456", "lin@uniask.local", "student");

        Course ds = createCourse("数据结构", "计算机科学与技术", "2026 春", "面向大一到大二的核心课程，聚焦线性表、树、图与复杂度分析。", teacher);
        Course db = createCourse("数据库系统", "软件工程", "2026 春", "围绕关系模型、SQL、事务隔离和数据库设计展开。", teacher);

        Question q1 = createQuestion(
                "红黑树和 AVL 树该怎么选？",
                "我知道它们都是平衡二叉树，但不太理解实际系统里为什么很多库更偏向红黑树。希望从旋转次数、查询效率和工程实现三个角度说明。",
                ds,
                student,
                false,
                "可以先抓住一个核心区别：AVL 追求更严格的平衡，所以查询通常略优；红黑树放宽了平衡条件，因此插入删除时调整更少，更适合高频更新场景。"
        );
        Question q2 = createQuestion(
                "第三范式和性能优化会冲突吗？",
                "做课程设计时老师要求满足第三范式，但我看到很多实际项目又会做冗余字段。什么时候该规范化，什么时候该为了性能做反规范化？",
                db,
                student,
                true,
                "第三范式适合保证一致性和可维护性，反规范化则用于解决已经确认存在的热点查询问题。先规范化建模，再基于真实查询瓶颈做有限、可控的冗余，是更稳妥的工程路径。"
        );

        createAnswer(
                q2,
                teacher,
                "可以把它理解成两个阶段：建模阶段优先保证结构正确，优化阶段再依据慢查询和访问模式做局部冗余。只要你能说明冗余字段如何同步，就不算违背设计原则。",
                true
        );

        createNotification(student,
                "欢迎来到 UniAsk",
                "演示数据已经准备好了，你可以直接用 student_lin / 123456 登录并体验提问流程。",
                "WELCOME",
                null);
        createNotification(teacher,
                "有新的演示问题待查看",
                "系统已经为你创建了两门课程，并准备了示例问答，方便直接演示教师工作流。",
                "DEMO_READY",
                q1.getId());
        createNotification(admin,
                "演示环境已初始化",
                "你可以使用 admin / 123456 登录管理中心，查看课程、问答和用户分布。",
                "DEMO_READY",
                null);
    }

    private User createUser(String username, String rawPassword, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Course createCourse(String name, String major, String semester, String description, User teacher) {
        Course course = new Course();
        course.setName(name);
        course.setMajor(major);
        course.setSemester(semester);
        course.setDescription(description);
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    private Question createQuestion(String title,
                                    String content,
                                    Course course,
                                    User student,
                                    boolean solved,
                                    String aiAnswer) {
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setCourse(course);
        question.setStudent(student);
        question.setIsSolved(solved);
        question.setAiAnswer(aiAnswer);
        return questionRepository.save(question);
    }

    private void createAnswer(Question question, User author, String content, boolean accepted) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAuthor(author);
        answer.setContent(content);
        answer.setIsAccepted(accepted);
        answerRepository.save(answer);
    }

    private void createNotification(User user, String title, String content, String type, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notificationRepository.save(notification);
    }
}
