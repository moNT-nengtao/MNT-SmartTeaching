package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.warning.WarningQueryDTO;
import com.smartteaching.common.vo.warning.WarningDetailVO;
import com.smartteaching.common.vo.warning.WarningListVO;
import com.smartteaching.common.vo.warning.WarningStatsVO;
import com.smartteaching.entity.warning.Warning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarningMapper extends BaseMapper<Warning> {
    /**
     * 分页查询预警列表
     * @param iPage
     * @param queryDTO
     * @param currentUserId 当前登录用户ID（学生时只看自己，教师/管理员传null看全部）
     * @return
     */
    IPage<WarningListVO> selectWarningPage(IPage<WarningListVO> iPage,
                                            @Param("dto") WarningQueryDTO queryDTO,
                                            @Param("currentUserId") Long currentUserId);

    /**
     * 查询预警详情
     * @param id
     * @return
     */
    WarningDetailVO selectWarningDetail(@Param("id") Long id);

    /**
     * 查询学生成绩趋势
     * @param id 预警记录ID
     * @return
     */
    List<WarningDetailVO.ScoreTrendData> selectScoreTrend(@Param("id") Long id);

    /**
     * 查询学生考勤数据
     * @param id 预警记录ID
     * @return
     */
    WarningDetailVO.AttendanceData selectAttendanceData(@Param("id") Long id);

    /**
     * 查询预警统计数据
     * @return
     */
    WarningStatsVO selectWarningStats();

    /**
     * 导出预警列表（不分页）
     * @param queryDTO
     * @param currentUserId 当前登录用户ID（学生时只看自己，教师/管理员传null看全部）
     * @return
     */
    List<WarningListVO> selectWarningExportList(@Param("dto") WarningQueryDTO queryDTO,
                                                 @Param("currentUserId") Long currentUserId);
}