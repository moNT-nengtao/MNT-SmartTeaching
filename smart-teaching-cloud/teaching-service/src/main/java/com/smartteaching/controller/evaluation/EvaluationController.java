package com.smartteaching.controller.evaluation;

import com.smartteaching.common.dto.evaluation.EvaluationSubmitDTO;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.evaluation.EvaluationCourseVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherRankingVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;
import com.smartteaching.service.evaluation.EvaluationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName EvaluationController
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:02
 **/
@RestController
@RequestMapping("/api/evaluation")
@Slf4j
public class EvaluationController {
    @Resource
    private EvaluationService evaluationService;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 可评价课程列表
     * @return
     */
    @GetMapping("/evaluable")
    public Result<List<EvaluationCourseVO>> getEvaluableCourses(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long studentId = jwtUtil.getUserIdFromHeader(authHeader);

        log.info("可评价课程列表: {}", studentId);

        List<EvaluationCourseVO> vo = evaluationService.getEvaluableCourses(studentId);
        return Result.success(vo);
    }

    /**
     * 提交课程评价
     * @param request
     * @return
     */
    @PostMapping
    public Result saveEvaluableCourse(HttpServletRequest request,
                                      @RequestBody EvaluationSubmitDTO evaluationSubmitDTO) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long studentId = jwtUtil.getUserIdFromHeader(authHeader);

        log.info("提交课程评价: {}", studentId);

        evaluationService.saveEvaluableCourse(studentId,evaluationSubmitDTO);
        return Result.success();
    }

    /**
     * 课程评价仪表盘
     * @param request
     * @return
     */
    @GetMapping("/dashboard")
    public Result<EvaluationTeacherVO> evaluationDashboard(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long teacherId = jwtUtil.getUserIdFromHeader(authHeader);

        log.info("课程评价统计: {}", teacherId);

        EvaluationTeacherVO dashboardEvaluationVO = evaluationService.evaluationDashboard(teacherId);
        return Result.success(dashboardEvaluationVO);
    }

    /**
     * 按课程查询评价列表（教师）
     * @param courseId
     * @return
     */
    @GetMapping("/list")
    public Result<List<EvaluationTeacherVO.EvaluationItem>> getEvaluationList(
            @RequestParam Long courseId) {
        log.info("按课程查询评价列表: courseId={}", courseId);

        List<EvaluationTeacherVO.EvaluationItem> list = evaluationService.getEvaluationList(courseId);
        return Result.success(list);
    }

    /**
     * 教师评价榜单
     * @param collegeId
     * @param subject
     * @return
     */
    @GetMapping("/ranking")
    public Result<List<EvaluationTeacherRankingVO>> getEvaluationRanking(@RequestParam(required = false) Long collegeId,
                                                                         @RequestParam(required = false) String subject) {
        log.info("教师评价榜单: collegeId={}, subject={}", collegeId, subject);

        List<EvaluationTeacherRankingVO> vo = evaluationService.getEvaluationRanking(collegeId,subject);
        return Result.success(vo);
    }



}
