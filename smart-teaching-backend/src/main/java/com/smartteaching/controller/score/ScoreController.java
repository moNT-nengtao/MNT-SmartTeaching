package com.smartteaching.controller.score;

import com.alibaba.excel.EasyExcel;
import com.smartteaching.common.dto.score.ScoreEnterDTO;
import com.smartteaching.common.dto.score.ScorePageReqDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.course.CourseScheduleExportVO;
import com.smartteaching.common.vo.score.*;
import com.smartteaching.entity.course.Course;
import com.smartteaching.service.score.ScoreService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @ClassName ScoreController
 * @Description 成绩管理控制器
 * @Author MNT
 * @Date 2026/8/27 11:18
 **/
@RestController
@RequestMapping("/api/score")
@Slf4j
public class ScoreController {
    @Resource
    private ScoreService scoreService;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 成绩统计
     * @return
     */
    @GetMapping("/stats")
    public Result<ScoreStatsQueryVO> getScoreStats(ScorePageReqDTO dto) {
        ScoreStatsQueryVO vo = scoreService.getScoreStats(dto);
        return Result.success(vo);
    }

    /**
     * 异常成绩分页列表
     * @param dto
     * @return
     */
    @GetMapping("/abnormal")
    public Result<PageResult<ScoreStatsQueryVO.AbnormalScore>> getAbnormalScores(ScorePageReqDTO dto) {
        PageResult<ScoreStatsQueryVO.AbnormalScore> pageResult = scoreService.getAbnormalScores(dto);
        return Result.success(pageResult);
    }

    /**
     * 导出异常成绩
     * @param dto
     */
    @GetMapping("/export")
    public void exportScore(ScorePageReqDTO dto,
                            HttpServletResponse response) throws IOException {
        log.info("异常成绩导出");
        String fileName = URLEncoder.encode("异常成绩导出.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")+ ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        if (response.isCommitted()) {
            log.warn("响应流已提交，终止导出操作");
            return;
        }

        try {
            //封装查询
            List<ScoreExportVO> data = scoreService.exportAbnormalScore(dto);
            if (data == null || data.isEmpty()) {
                log.info("查询不到符合条件的异常成绩数据，学期={}，学院ID={}", dto.getSemester(), dto.getCollegeId());
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":200,\"msg\":\"没有找到符合条件的异常成绩数据\"}");
                return;
            }
            //写出
            EasyExcel.write(response.getOutputStream(), ScoreExportVO.class)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("异常成绩数据")
                    .doWrite(data);
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            }
            log.error("成绩导出失败，查询条件：学期={}，学院ID={}", dto.getSemester(), dto.getCollegeId(), e);
        }
    }

    /**
     * 我的成绩(学生端)
     * @param dto
     * @return
     */
    @GetMapping("/my")
    public Result<List<ScoreMyQueryVO>> scoreMyQuery(ScorePageReqDTO dto,
                                                     HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long studentId = jwtUtil.getUserIdFromHeader(authHeader);

        log.info("我的成绩,学期:{},studentId:{}",dto.getSemester(), studentId);
        List<ScoreMyQueryVO> pageResult = scoreService.scoryMyQueryPage(dto, studentId);
        return Result.success(pageResult);
    }

    /**
     * 获取课程成绩列表（教师录入用）
     * @param courseId
     * @return
     */
    @GetMapping("/course/{courseId}")
    public Result<List<ScoreCourseQueryVO>> getCourseScoreList(@PathVariable Long courseId) {
        log.info("获取课程成绩列表: courseId={}", courseId);
        List<ScoreCourseQueryVO> scoreList = scoreService.getCourseScoreList(courseId);
        return Result.success(scoreList);
    }

    /**
     * 录入成绩
     * @param dto
     * @return
     */
    @PostMapping("/enter")
    public Result enterScore(@Validated @RequestBody ScoreEnterDTO dto) {
        log.info("录入成绩: courseId={}, 学生数量={}", dto.getCourseId(), dto.getScores().size());
        scoreService.enterScore(dto);
        return Result.success();
    }

    /**
     * 批量导出成绩
     * @param
     */
    @GetMapping("/course/{courseId}/export")
    public void exportScoreCourse(@PathVariable Long courseId,
                                  @RequestParam(required = false) String courseName,
                                  HttpServletResponse response) throws IOException {
        // 文件名编码
        String fileName = URLEncoder.encode("课表导出.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        if (response.isCommitted()) {
            log.warn("响应流已提交，终止导出操作");
            return;
        }

        try {
            List<ScoreCourseExportVO> data = scoreService.exportCourseScore(courseId, courseName);
            if (data == null || data.isEmpty()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":400,\"msg\":\"暂无导出数据\"}");
                return;
            }

            EasyExcel.write(response.getOutputStream(), ScoreCourseExportVO.class)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("成绩数据")
                    .doWrite(data);

            log.info("成绩导出成功，课程ID: {}, 课程名称: {}, 数据量: {}", courseId, courseName, data.size());

        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            }
            log.error("成绩导出失败，课程ID: {}, 课程名称: {}", courseId, courseName, e);
        }
    }

}
