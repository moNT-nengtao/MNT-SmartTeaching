package com.smartteaching.controller.selection;

import com.smartteaching.common.dto.selection.SelectionQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.selection.SelectionConfigQueryVO;
import com.smartteaching.common.vo.selection.SelectionMyCourseVO;
import com.smartteaching.common.vo.selection.SelectionQueryVO;
import com.smartteaching.common.vo.selection.SelectionStudentVO;
import com.smartteaching.entity.selection.SelectionConfig;
import com.smartteaching.service.selection.SelectionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SelectionController
 * @Description 选课管理控制器
 * @Author MNT
 * @Date 2026/8/17 09:14
 **/
@RestController
@RequestMapping("/api/selection")
@Slf4j
public class SelectionController {
    /**
     * TODO 查询剩余名额、选课热门预警不知道在哪
     */

    @Resource
    private SelectionService selectionService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 获取选课配置
     * @return
     */
    @GetMapping("/config")
    public Result<SelectionConfigQueryVO> getSelectionConfig() {
        log.info("获取选课配置");
        SelectionConfigQueryVO selectionConfigQueryVO = selectionService.getSelectionConfig();
        return Result.success(selectionConfigQueryVO);
    }


    /**
     * 设置选课时间
     * @param params
     * @return
     */
    @PostMapping("/time")
    public Result saveSelectionTime(@RequestBody Map<String, Object> params) {
        log.info("设置选课时间");
        try {
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");
            String scopeType = (String) params.get("scope");
            String scopeValue = (String) params.get("scopeValue");

            if (startTime == null || endTime == null) {
                return Result.error("开始时间和结束时间不能为空");
            }

            SelectionConfig config = new SelectionConfig();
            config.setId(1L);
            config.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            config.setEndTime(LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            config.setScopeType(scopeType);
            config.setScopeValue(scopeValue);

            selectionService.saveSelectionConfig(config);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 选课大厅
     * @param selectionQueryDTO
     * @return
     */
    @GetMapping("/course/list")
    public Result<PageResult<SelectionQueryVO>> getSelectionQuery(SelectionQueryDTO selectionQueryDTO,
                                                                  HttpServletRequest request) {
        log.info("选课大厅:{}", selectionQueryDTO);
        String token = request.getHeader("Authorization");
        Long userId = jwtUtil.getUserIdFromHeader(token);
        PageResult<SelectionQueryVO> voPageResult = selectionService.getSelectionQuery(selectionQueryDTO,userId);
        return Result.success(voPageResult);
    }

    /**
     * 课程选课学生名单
     * @param courseId
     * @return
     */
    @GetMapping("/{courseId}/students")
    public Result<PageResult<SelectionStudentVO>>  getSelectionStudent(@PathVariable Long courseId,
                                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("课程选课学生名单:{}", courseId);
        PageResult<SelectionStudentVO> voPageResult = selectionService.getSelectionStudernt(courseId, pageNum, pageSize);
        return Result.success(voPageResult);
    }

    /**
     * 智能推荐课程
     * @param
     * @return
     */
    @GetMapping("/recommend")
    public Result<List<SelectionQueryVO>> getSelectionRecommend(HttpServletRequest request) {
        log.info("智能推荐课程:{}", request);
        //拿用户id做智能分析
        String token = request.getHeader("Authorization");
        Long userId = jwtUtil.getUserIdFromHeader(token);

        List<SelectionQueryVO> pageResult = selectionService.getRecommendCourses(userId);

        return Result.success(pageResult);
    }

    /**
     * 学生选课
     * TODO 本类型项目难点：并发选课情况（还未处理，考虑放到redis）
     * @param courseId
     * @return
     */
    @PostMapping("/select/{courseId}")
    public Result studentSaveCourses(@PathVariable Long courseId,
                                    HttpServletRequest request) {
        log.info("学生选课:{}", courseId);
        String token = request.getHeader("Authorization");
        Long studentId = jwtUtil.getUserIdFromHeader(token);

        selectionService.studentSaveCourses(courseId,studentId);

        return Result.success();
    }

    /**
     * 学生退课
     * @param courseId
     * @param request
     * @return
     */
    @DeleteMapping("/drop/{courseId}")
    public Result studentDeleteCourses(@PathVariable Long courseId,
                                       HttpServletRequest request) {
        log.info("学生退课:{}", courseId);

        String token = request.getHeader("Authorization");
        Long studentId = jwtUtil.getUserIdFromHeader(token);

        selectionService.studentDeleteCourses(courseId,studentId);

        return Result.success();
    }

    /**
     * 我的已选课程
     * @param request
     * @return
     */
    @GetMapping("/my")
    public Result<PageResult<SelectionMyCourseVO>> getSelectionMyCourses(HttpServletRequest request,
                                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("我的已选课程");
        String token = request.getHeader("Authorization");
        Long studentId = jwtUtil.getUserIdFromHeader(token);

        PageResult<SelectionMyCourseVO> pageResult = selectionService.getSelectionMyCourses(studentId,pageNum,pageSize);
        return Result.success(pageResult);
    }

}
