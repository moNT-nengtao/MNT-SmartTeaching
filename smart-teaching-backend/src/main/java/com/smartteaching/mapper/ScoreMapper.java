package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.score.ScorePageReqDTO;
import com.smartteaching.common.vo.score.*;
import com.smartteaching.entity.score.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
    /**
     * 查询统计卡片总数据：总人数、平均分、及格人数、挂科人数
     */
    ScoreStatsQueryVO.StatCardData selectStatCard(@Param("dto") ScorePageReqDTO dto);

    /**
     * 查询成绩分段饼图原始计数
     */
    ScoreStatsQueryVO.ScoreDistribution selectScoreDistribution(@Param("dto") ScorePageReqDTO dto);

    /**
     * 查询各学院平均分对比
     */
    List<ScoreStatsQueryVO.CollegeScoreCompare.Item> selectCollegeAvgScore(@Param("dto") ScorePageReqDTO dto);

    /**
     * 异常成绩总条数
     */
    IPage<ScoreStatsQueryVO.AbnormalScore> selectAbnormalScorePage(IPage<ScoreStatsQueryVO.AbnormalScore> page, @Param("dto") ScorePageReqDTO dto);

    /**
     * 导出异常成绩
     */
    List<ScoreExportVO> selectExportList(@Param("dto") ScorePageReqDTO dto);

    /**
     * 我的成绩
     */
    List<ScoreMyQueryVO> selectMyScore(@Param("dto") ScorePageReqDTO dto,@Param("studentId") Long studentId);

    /**
     * 该课程所有学生的成绩列表
     */
    List<ScoreCourseQueryVO> selectCourseScoreList(Long courseId);

    /**
     * 获取课程的授课教师ID
     */
    int insertOrUpdateBatch(Long courseId, List<Score> scores);

    /**
     * 批量导出成绩
     */
    List<ScoreCourseExportVO> selectScoreCourseExportList(@Param("courseId") Long courseId,
                                                          @Param("courseName") String courseName);
}