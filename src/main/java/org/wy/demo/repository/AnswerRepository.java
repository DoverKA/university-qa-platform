package org.wy.demo.repository;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    // 根据问题查询所有回答
    List<Answer> findByQuestion(Question question);

    // 根据回答者查询回答
    List<Answer> findByAuthor(User author);

    // 查询问题的最佳答案
    Answer findByQuestionAndIsAccepted(Question question, Boolean isAccepted);
}

