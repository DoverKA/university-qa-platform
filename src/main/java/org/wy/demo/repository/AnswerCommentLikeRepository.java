package org.wy.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wy.demo.entity.AnswerCommentLike;

import java.util.List;
import java.util.Optional;

public interface AnswerCommentLikeRepository extends JpaRepository<AnswerCommentLike, Integer> {
    List<AnswerCommentLike> findByCommentIdIn(List<Integer> commentIds);

    Optional<AnswerCommentLike> findByCommentIdAndUserId(Integer commentId, Integer userId);

    void deleteByCommentIdIn(List<Integer> commentIds);
}