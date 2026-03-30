package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.wy.demo.entity.Notification;
import org.wy.demo.entity.User;
import org.wy.demo.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    public void notifyUser(Integer userId, String title, String content, String type, Integer relatedId) {
        if (userId == null) {
            return;
        }
        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public Notification markRead(Integer notificationId, Integer operatorId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return null;
        }
        if (!notification.getUser().getId().equals(operatorId)) {
            throw new AccessDeniedException("You can only mark your own notifications as read");
        }
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public int markAllRead(Integer operatorId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(operatorId);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }
}
