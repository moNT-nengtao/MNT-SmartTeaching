package com.smartteaching.service.evaluation;

import com.smartteaching.common.dto.evaluation.EvaluationSubmitDTO;
import com.smartteaching.common.vo.evaluation.EvaluationCourseVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherRankingVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;

import java.util.List;

/**
 * @ClassName EvaluationService
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:03
 **/
public interface EvaluationService {

    /**
     * 可评价课程列表
     *
     * @param studentId
     * @return
     */
    List<EvaluationCourseVO> getEvaluableCourses(Long studentId);

    /**
     * 提交课程评价
     * @param studentId
     * @param evaluationSubmitDTO
     */
    void saveEvaluableCourse(Long studentId, EvaluationSubmitDTO evaluationSubmitDTO);

    /**
     * 课程评价统计
     * @param teacherId
     * @return
     */
    EvaluationTeacherVO evaluationDashboard(Long teacherId);

    /**
     * 按课程查询评价列表（教师）
     * @param courseId
     * @return
     */
    List<EvaluationTeacherVO.EvaluationItem> getEvaluationList(Long courseId);

    /**
     * 教师评价榜单
     * @param collegeId
     * @param subject
     * @return
     */
    List<EvaluationTeacherRankingVO> getEvaluationRanking(Long collegeId, String subject);
}
