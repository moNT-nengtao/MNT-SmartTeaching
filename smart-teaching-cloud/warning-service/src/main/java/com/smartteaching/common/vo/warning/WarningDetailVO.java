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
        /** 考勤成功 */
        private Integer attended;
        /** 迟到 */
        private Integer late;
        /** 请假 */
        private Integer leaveCount;
        /** 缺勤（未落定） */
        private Integer absent;
        /** 旷课（落定缺勤） */
        private Integer truant;

        public Double getAttendanceRate() {
            if (totalSessions == null || totalSessions == 0) {
                return 0.0;
            }
            int att = (attended != null ? attended : 0) + (late != null ? late : 0);
            return (double) att / totalSessions * 100;
        }
    }
}