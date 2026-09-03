package com.smartteaching.common.constant;

/**
 * @ClassName AttendanceStatus
 * @Description 考勤状态常量（attendance_record.status 六态）
 * @Author MNT
 * @Date 2026/9/2
 **/
public class AttendanceStatus {

    /** 缺勤：创建签到会话时的初始状态 */
    public static final int ABSENT = 0;

    /** 考勤成功：签到有效期内学生签到成功 */
    public static final int PRESENT = 1;

    /** 迟到：教师手动标记 */
    public static final int LATE = 2;

    /** 请假：教师手动标记 */
    public static final int LEAVE = 3;

    /** 旷课：会话结束后仍未签到且未被标记为请假 */
    public static final int TRUANT = 4;

    /** 手动签到：教师代学生签到（学生到场但无法自主签到），特殊状态留痕，区别于考勤成功 */
    public static final int MANUAL = 5;

    /** 教师可手动修改的状态（迟到/请假/旷课，不允许改成考勤成功） */
    public static final int[] TEACHER_MODIFIABLE = {LATE, LEAVE, TRUANT};

    private AttendanceStatus() {
    }

    /**
     * 考勤状态文本
     */
    public static String text(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case ABSENT:
                return "缺勤";
            case PRESENT:
                return "考勤成功";
            case LATE:
                return "迟到";
            case LEAVE:
                return "请假";
            case TRUANT:
                return "旷课";
            case MANUAL:
                return "手动签到";
            default:
                return "未知";
        }
    }
}
