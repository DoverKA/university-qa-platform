package org.wy.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sync.sql")
public class SqlSyncProperties {

    /**
     * 是否启用 SQL 同步。
     */
    private boolean enabled = false;

    /**
     * 应用启动后自动执行一次同步。
     */
    private boolean runOnStartup = false;

    /**
     * 教师同步 SQL，要求返回至少一列：username。
     */
    private String teacherQuery = "SELECT username, email FROM teacher";

    /**
     * 默认教师密码（首次同步创建教师账号使用）。
     */
    private String defaultTeacherPassword = "123456";

    /**
     * 课程同步 SQL，要求返回：name, major, semester, teacher_username，description 可选。
     */
    private String courseQuery = "SELECT name, major, semester, teacher_username, description FROM external_course";
}
