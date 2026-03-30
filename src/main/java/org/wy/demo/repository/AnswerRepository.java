package org.wy.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByQuestion(Question question);

    List<Answer> findByAuthor(User author);

    Answer findByQuestionAndIsAccepted(Question question, Boolean isAccepted);

    List<Answer> findByContentContainingIgnoreCase(String keyword);
}
