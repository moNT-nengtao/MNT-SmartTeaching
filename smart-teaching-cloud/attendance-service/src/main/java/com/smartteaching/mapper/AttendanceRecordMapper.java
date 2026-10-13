package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.attendance.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @ClassName AttendanceRecordMapper
 * @Description 考勤记录Mapper，对应attendance_record表
 * @Author MNT
 * @Date 2026/9/2
 **/
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    /**
     * 学生签到成功：仅当记录仍处于缺勤(0)时更新为考勤成功(1)，防止覆盖教师已标记的迟到/请假
     * @return 影响行数，0表示状态已被其他操作修改
     */
    int checkinSuccess(@Param("id") Long id,
                       @Param("sessionId") Long sessionId,
                       @Param("studentId") Long studentId,
                       @Param("checkinTime") LocalDateTime checkinTime,
                       @Param("longitude") java.math.BigDecimal longitude,
                       @Param("latitude") java.math.BigDecimal latitude);

    /**
     * 结束会话：将该会话下所有缺勤(0)记录落定为旷课(4)
     * @return 影响行数
     */
    int batchFinalizeTruant(@Param("sessionId") Long sessionId);

    /**
     * 教师手动签到：学生到场但无法自主签到，教师代签为手动签到(5)（特殊状态留痕）。
     * 仅当记录仍为缺勤(0)时更新，防止覆盖已存在的考勤成功/迟到/请假/旷课
     * @return 影响行数，0表示记录状态不是缺勤或已不存在
     */
    int manualCheckin(@Param("id") Long id,
                      @Param("sessionId") Long sessionId,
                      @Param("studentId") Long studentId,
                      @Param("checkinTime") LocalDateTime checkinTime);

    /**
     * 统计学生在指定会话中的考勤记录条数（用于幂等校验）
     */
    Long countBySessionAndStudent(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 批量插入考勤记录（创建会话时一次写入本课程全部学生，初始状态缺勤0）
     */
    int batchInsertRecords(@Param("records") java.util.List<AttendanceRecord> records);
}
