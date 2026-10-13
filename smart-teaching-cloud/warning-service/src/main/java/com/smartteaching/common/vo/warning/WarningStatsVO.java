package com.smartteaching.common.vo.warning;

import lombok.Data;

/**
 * @ClassName WarningStatsVO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:43
 **/
@Data
public class WarningStatsVO {

    private Long absentCount;

    private Long failCount;

    private Long homeworkCount;

    private Long totalCount;

    private Long highLevelCount;

    private Long mediumLevelCount;

    private Long lowLevelCount;
}