package org.wy.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wy.demo.entity.AnswerComment;

import java.util.List;

public interface AnswerCommentRepository extends JpaRepository<AnswerComment, Integer> {
    List<AnswerComment> findByAnswerIdOrderByCreateTimeAsc(Integer answerId);

    List<AnswerComment> findByAnswerIdIn(List<Integer> answerIds);
}