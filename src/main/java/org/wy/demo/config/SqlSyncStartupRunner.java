package org.wy.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.wy.demo.service.SqlSyncService;

import java.util.Map;

@Component
public class SqlSyncStartupRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SqlSyncStartupRunner.class);

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
            Map<String, Integer> result = sqlSyncService.syncTeachersAndCourses();
            log.info(
                    "SQL sync completed: teachersCreated={}, coursesCreated={}, coursesSkipped={}",
                    result.getOrDefault("teachersCreated", 0),
                    result.getOrDefault("coursesCreated", 0),
                    result.getOrDefault("coursesSkipped", 0)
            );
        } catch (Exception e) {
            log.error("SQL sync failed: {}", e.getMessage(), e);
        }
    }
}
