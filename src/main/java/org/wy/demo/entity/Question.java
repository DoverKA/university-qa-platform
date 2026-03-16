package org.wy.demo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 问题标题（必填，高校场景：简洁描述问题）
    @Column(nullable = false, length = 200)
    private String title;

    // 问题详情（必填，详细描述问题）
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 关联课程（必填，提问必须归属某门课程）
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 提问学生（必填）
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // 是否已解决（默认false）
    @Column(name = "is_solved")
    private Boolean isSolved = false;

    // 创建时间（自动填充）
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // AI回答内容（可选，后续对接AI用）
    @Column(name = "ai_answer", columnDefinition = "TEXT")
    private String aiAnswer;

    // 保存前自动填充创建时间
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        if (this.isSolved == null) {
            this.isSolved = false; // 默认未解决
        }
    }
}