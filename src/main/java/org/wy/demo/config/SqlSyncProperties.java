package org.wy.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sync.sql")
public class SqlSyncProperties {
    private boolean enabled = false;
    private boolean runOnStartup = false;
    private String teacherQuery = "SELECT username, email FROM teacher";
    private String defaultTeacherPassword = "123456";
    private String courseQuery = "SELECT name, major, semester, teacher_username, description FROM external_course";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRunOnStartup() {
        return runOnStartup;
    }

    public void setRunOnStartup(boolean runOnStartup) {
        this.runOnStartup = runOnStartup;
    }

    public String getTeacherQuery() {
        return teacherQuery;
    }

    public void setTeacherQuery(String teacherQuery) {
        this.teacherQuery = teacherQuery;
    }

    public String getDefaultTeacherPassword() {
        return defaultTeacherPassword;
    }

    public void setDefaultTeacherPassword(String defaultTeacherPassword) {
        this.defaultTeacherPassword = defaultTeacherPassword;
    }

    public String getCourseQuery() {
        return courseQuery;
    }

    public void setCourseQuery(String courseQuery) {
        this.courseQuery = courseQuery;
    }
}
