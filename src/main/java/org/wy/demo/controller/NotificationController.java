package org.wy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wy.demo.entity.Notification;
import org.wy.demo.security.SecurityUtils;
import org.wy.demo.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> getMyNotifications() {
        return notificationService.getNotificationsForUser(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/summary")
    public Map<String, Long> getNotificationSummary() {
        return Map.of("unreadCount", notificationService.getUnreadCount(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Integer id) {
        return notificationService.markRead(id, SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/read-all")
    public Map<String, Integer> markAllRead() {
        return Map.of("updated", notificationService.markAllRead(SecurityUtils.getCurrentUserId()));
    }
}
