package org.wy.demo.dto;

import lombok.Data;

@Data
public class SqlSyncResult {
    private int teachersCreated;
    private int coursesCreated;
    private int coursesSkipped;
}
