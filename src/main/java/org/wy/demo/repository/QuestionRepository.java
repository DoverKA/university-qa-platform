package org.wy.demo.repository;

import org.wy.demo.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 按课程ID查询问题（高校核心：学生看本课程的问题）
    List<Question> findByCourseId(Integer courseId);

    // 按课程ID+是否已解决查询（筛选“未解决/已解决”问题）
    List<Question> findByCourseIdAndIsSolved(Integer courseId, Boolean isSolved);

    // 按学生ID查询（学生看自己提的问题）
    List<Question> findByStudentId(Integer studentId);

    // 按课程ID+关键词模糊查询（搜索课程内的问题）
    List<Question> findByCourseIdAndTitleContaining(Integer courseId, String keyword);
}