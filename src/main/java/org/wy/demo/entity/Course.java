package org.wy.demo.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Course") // 修正笔误：Coures → Course（避免数据库表名错误）
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(length = 50, nullable = false)
    private String major;

    @Column(length = 20, nullable = false)
    private String semester;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist(){
        this.createTime = LocalDateTime.now();
    }
}