package org.wy.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.wy.demo.dto.SqlSyncResult;
import org.wy.demo.service.SqlSyncService;

@Slf4j
@Component
public class SqlSyncStartupRunner implements CommandLineRunner {

    @Autowired
    private SqlSyncProperties sqlSyncProperties;

    @Autowired
    private SqlSyncService sqlSyncService;

    @Override
    public void run(String... args) {
        if (!sqlSyncProperties.isEnabled() || !sqlSyncProperties.isRunOnStartup()) {
            return;
        }

        try {
            SqlSyncResult result = sqlSyncService.syncTeachersAndCourses();
            log.info("SQL 自动同步完成: teachersCreated={}, coursesCreated={}, coursesSkipped={}",
                    result.getTeachersCreated(), result.getCoursesCreated(), result.getCoursesSkipped());
        } catch (Exception e) {
            log.error("SQL 自动同步失败: {}", e.getMessage(), e);
        }
    }
}
