package com.smartteaching.common.vo.attendance;

import lombok.Data;

/**
 * @ClassName AttendanceStatsVO
 * @Description 考勤统计VO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceStatsVO {

    /** 应到人数 */
    private Long total;

    /** 考勤成功 */
    private Long present;

    /** 迟到 */
    private Long late;

    /** 请假 */
    private Long leave;

    /** 缺勤（未落定的初始状态） */
    private Long absent;

    /** 旷课 */
    private Long truant;

    /** 手动签到（教师代签，特殊状态留痕） */
    private Long manual;

    /** 出勤率（考勤成功+迟到+手动签到）/应到 *100 */
    private Double rate;
}
