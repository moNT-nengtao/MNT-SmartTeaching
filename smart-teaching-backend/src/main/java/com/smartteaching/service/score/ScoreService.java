package com.smartteaching.service.score;

import com.smartteaching.common.dto.score.ScoreEnterDTO;
import com.smartteaching.common.dto.score.ScorePageReqDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.score.*;


import java.util.List;

/**
 * @ClassName ScoreService
 * @Description 成绩服务接口
 * @Author MNT
 * @Date 2026/8/27 11:17
 **/
public interface ScoreService {

    /**
     * 获取成绩统计：统计卡片+饼图+柱状图
     */
    ScoreStatsQueryVO getScoreStats(ScorePageReqDTO dto);

    /**
     * 分页查询异常成绩
     */
    PageResult<ScoreStatsQueryVO.AbnormalScore> getAbnormalScores(ScorePageReqDTO dto);

    /**
     * 导出异常成绩
     *
     * @return
     */
    List<ScoreExportVO> exportAbnormalScore(ScorePageReqDTO dto);

    /**
     * 我的成绩
     *
     * @param dto
     * @param studentId
     * @return
     */
    List<ScoreMyQueryVO> scoryMyQueryPage(ScorePageReqDTO dto, Long studentId);

    /**
     * 获取课程成绩列表（教师录入用）
     * @param courseId
     * @return
     */
    List<ScoreCourseQueryVO> getCourseScoreList(Long courseId);

    /**
     * 录入成绩
     * @param dto
     */
    void enterScore(ScoreEnterDTO dto);

    /**
     * 导出成绩
     * @param courseId
     * @param courseName
     * @return
     */
    List<ScoreCourseExportVO> exportCourseScore(Long courseId, String courseName);
}
