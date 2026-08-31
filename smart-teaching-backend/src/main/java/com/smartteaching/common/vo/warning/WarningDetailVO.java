package com.smartteaching.common.vo.warning;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName WarningDetailVO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:42
 **/
@Data
public class WarningDetailVO {

    // 学生基本信息
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private String collegeName;
    private String majorName;

    // 预警信息
    private Long warningId;
    private String reason;
    private String warningType;
    private String level;
    private Integer levelCode;
    private Integer warningStatus;
    private LocalDateTime createTime;

    // 成绩趋势数据
    private List<ScoreTrendData> scoreTrend;

    // 考勤数据
    private AttendanceData attendance;

    // 改进建议
    private List<String> suggestions;

    @Data
    public static class ScoreTrendData {
        private String courseName;
        private Double score;
        private String semester;
    }

    @Data
    public static class AttendanceData {
        private Integer totalSessions;
        private Integer attended;
        private Integer absent;

        public Double getAttendanceRate() {
            if (totalSessions == null || totalSessions == 0) {
                return 0.0;
            }
            return attended != null ? (double) attended / totalSessions * 100 : 0.0;
        }
    }
}