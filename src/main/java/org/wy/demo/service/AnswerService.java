package org.wy.demo.service;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.Question;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionService questionService;

    // 提交回答
    public Answer saveAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    // 根据ID查询回答
    public Answer getAnswerById(Integer id) {
        return answerRepository.findById(id).orElse(null);
    }

    // 根据问题查询所有回答
    public List<Answer> getAnswersByQuestion(Question question) {
        return answerRepository.findByQuestion(question);
    }

    // 采纳最佳答案（同时标记问题为已解决）
    public Answer acceptAnswer(Integer answerId) {
        Answer answer = getAnswerById(answerId);
        if (answer != null) {
            // 取消该问题原有最佳答案
            Answer oldAccepted = answerRepository.findByQuestionAndIsAccepted(answer.getQuestion(), true);
            if (oldAccepted != null) {
                oldAccepted.setIsAccepted(false);
                answerRepository.save(oldAccepted);
            }
            // 设置新的最佳答案
            answer.setIsAccepted(true);
            answerRepository.save(answer);
            // 标记问题为已解决
            questionService.markQuestionAsSolved(answer.getQuestion().getId());
            return answer;
        }
        return null;
    }

    // 删除回答
    public void deleteAnswer(Integer id) {
        answerRepository.deleteById(id);
    }
}

