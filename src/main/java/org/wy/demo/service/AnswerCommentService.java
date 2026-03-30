package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wy.demo.dto.AnswerCommentRequest;
import org.wy.demo.dto.AnswerCommentResponse;
import org.wy.demo.entity.Answer;
import org.wy.demo.entity.AnswerComment;
import org.wy.demo.entity.AnswerCommentLike;
import org.wy.demo.entity.User;
import org.wy.demo.repository.AnswerCommentLikeRepository;
import org.wy.demo.repository.AnswerCommentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnswerCommentService {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AnswerCommentRepository answerCommentRepository;

    @Autowired
    private AnswerCommentLikeRepository answerCommentLikeRepository;

    public List<AnswerCommentResponse> getCommentsByAnswerId(Integer answerId, Integer currentUserId) {
        Answer answer = answerService.getAnswerById(answerId);
        if (answer == null) {
            throw new IllegalArgumentException("Answer not found");
        }
        return buildTree(answerCommentRepository.findByAnswerIdOrderByCreateTimeAsc(answerId), currentUserId);
    }

    @Transactional
    public AnswerCommentResponse createComment(Integer answerId, Integer currentUserId, AnswerCommentRequest request) {
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("Comment content is required");
        }

        Answer answer = answerService.getAnswerById(answerId);
        if (answer == null) {
            throw new IllegalArgumentException("Answer not found");
        }

        User author = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AnswerComment parent = null;
        if (request.getParentId() != null) {
            parent = answerCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!parent.getAnswer().getId().equals(answerId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this answer");
            }
        }

        AnswerComment comment = new AnswerComment();
        comment.setAnswer(answer);
        comment.setAuthor(author);
        comment.setParentId(parent != null ? parent.getId() : null);
        comment.setContent(request.getContent().trim());
        AnswerComment saved = answerCommentRepository.save(comment);

        notifyParticipants(answer, saved, author, parent);
        return mapComments(List.of(saved), currentUserId).get(0);
    }

    @Transactional
    public AnswerCommentResponse toggleLike(Integer commentId, Integer currentUserId) {
        AnswerComment comment = answerCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        answerCommentLikeRepository.findByCommentIdAndUserId(commentId, currentUserId)
                .ifPresentOrElse(answerCommentLikeRepository::delete, () -> {
                    AnswerCommentLike like = new AnswerCommentLike();
                    like.setComment(comment);
                    like.setUser(user);
                    answerCommentLikeRepository.save(like);
                });

        return mapComments(List.of(comment), currentUserId).get(0);
    }

    @Transactional
    public void deleteByAnswerIds(List<Integer> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return;
        }
        List<AnswerComment> comments = answerCommentRepository.findByAnswerIdIn(answerIds);
        List<Integer> commentIds = comments.stream().map(AnswerComment::getId).collect(Collectors.toList());
        if (!commentIds.isEmpty()) {
            answerCommentLikeRepository.deleteByCommentIdIn(commentIds);
            answerCommentRepository.deleteAll(comments);
        }
    }

    private void notifyParticipants(Answer answer, AnswerComment comment, User author, AnswerComment parent) {
        if (parent != null && parent.getAuthor() != null && !parent.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(
                    parent.getAuthor().getId(),
                    "New reply received",
                    String.format("%s replied to your comment.", author.getUsername()),
                    "COMMENT_REPLY",
                    comment.getId()
            );
            return;
        }

        if (answer.getAuthor() != null && !answer.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(
                    answer.getAuthor().getId(),
                    "New comment received",
                    String.format("%s commented on your answer under question '%s'.", author.getUsername(), answer.getQuestion().getTitle()),
                    "ANSWER_COMMENT",
                    comment.getId()
            );
        }
    }

    private List<AnswerCommentResponse> buildTree(List<AnswerComment> comments, Integer currentUserId) {
        Map<Integer, AnswerCommentResponse> mapped = mapComments(comments, currentUserId).stream()
                .collect(Collectors.toMap(AnswerCommentResponse::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        List<AnswerCommentResponse> roots = new ArrayList<>();
        for (AnswerCommentResponse item : mapped.values()) {
            if (item.getParentId() == null) {
                roots.add(item);
            } else {
                AnswerCommentResponse parent = mapped.get(item.getParentId());
                if (parent != null) {
                    parent.getReplies().add(item);
                } else {
                    roots.add(item);
                }
            }
        }
        return roots;
    }

    private List<AnswerCommentResponse> mapComments(List<AnswerComment> comments, Integer currentUserId) {
        List<Integer> commentIds = comments.stream().map(AnswerComment::getId).collect(Collectors.toList());
        List<AnswerCommentLike> likes = commentIds.isEmpty() ? List.of() : answerCommentLikeRepository.findByCommentIdIn(commentIds);

        Map<Integer, Long> likeCountMap = likes.stream()
                .collect(Collectors.groupingBy(like -> like.getComment().getId(), Collectors.counting()));
        Set<Integer> likedIds = likes.stream()
                .filter(like -> like.getUser() != null && like.getUser().getId().equals(currentUserId))
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());

        return comments.stream().map(comment -> {
            AnswerCommentResponse response = new AnswerCommentResponse();
            response.setId(comment.getId());
            response.setAnswerId(comment.getAnswer().getId());
            response.setParentId(comment.getParentId());
            response.setAuthorId(comment.getAuthor().getId());
            response.setAuthorName(comment.getAuthor().getUsername());
            response.setAuthorRole(comment.getAuthor().getRole());
            response.setContent(comment.getContent());
            response.setCreateTime(comment.getCreateTime());
            response.setLikeCount(likeCountMap.getOrDefault(comment.getId(), 0L).intValue());
            response.setLikedByCurrentUser(likedIds.contains(comment.getId()));
            return response;
        }).collect(Collectors.toList());
    }
}