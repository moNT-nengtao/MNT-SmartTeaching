package com.smartteaching.common.dto.course;

import lombok.Data;

@Data
public class BatchConflictResultDTO {
    private String tempId; // 前端临时 id
    private String conflict; // 冲突描述，null 或 空 表示无冲突
}
