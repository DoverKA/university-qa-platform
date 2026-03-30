package org.wy.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wy.demo.entity.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByCourseId(Integer courseId);

    List<Question> findByCourseIdAndIsSolved(Integer courseId, Boolean isSolved);

    List<Question> findByStudentId(Integer studentId);

    List<Question> findByCourseIdAndTitleContaining(Integer courseId, String keyword);

    List<Question> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);

    long countByIsSolved(Boolean isSolved);
}
