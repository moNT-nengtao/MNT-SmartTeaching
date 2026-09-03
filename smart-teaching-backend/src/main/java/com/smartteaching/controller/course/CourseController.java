package com.smartteaching.controller.course;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartteaching.common.dto.course.BatchConflictResultDTO;
import com.smartteaching.common.dto.course.CourseQueryDTO;
import com.smartteaching.common.dto.course.CourseSaveDTO;
import com.smartteaching.common.dto.schedule.ScheduleConflictDTO;
import com.smartteaching.common.dto.schedule.ScheduleQueryDTO;
import com.smartteaching.common.dto.schedule.ScheduleSaveDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.course.CourseQueryVO;
import com.smartteaching.common.vo.course.CourseScheduleExportVO;
import com.smartteaching.common.vo.course.CourseScheduleQueryVO;
import com.smartteaching.service.course.CourseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CourseController
 * @Description
 * @Author MNT
 * @Date 2026/8/29 21:13
 **/
@RestController
@RequestMapping("/api/course")
@Slf4j
public class CourseController {

    @Autowired
    private CourseService courseService;


    //课程列表

    /**
     * 课程列表
     * @param courseQueryDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult<CourseQueryVO>> getCourseList(CourseQueryDTO courseQueryDTO) {
        log.info("课程列表: courseQueryDTO={}", courseQueryDTO);
        PageResult<CourseQueryVO> pageResult = courseService.getCourseList(courseQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 新增课程
     * @param courseSaveDTO
     * @return
     */
    @PostMapping
    public Result courseAdd(@Validated(CourseSaveDTO.AddGroup.class) @RequestBody CourseSaveDTO courseSaveDTO) {
        log.info("新增课程:{}", courseSaveDTO);
        courseService.addCourse(courseSaveDTO);
        return Result.success();
    }

    /**
     * 编辑课程
     * @param courseSaveDTO
     * @return
     */
    @PutMapping
    public Result courseUpdate(@Validated(CourseSaveDTO.EditGroup.class) @RequestBody CourseSaveDTO courseSaveDTO) {
        log.info("编辑课程：{}", courseSaveDTO);
        courseService.updateCourse(courseSaveDTO);
        return Result.success();
    }

    /**
     * 删除课程
     * @param courseId
     * @return
     */
    @DeleteMapping("/{id}")
    public Result courseDelete(@PathVariable("id") Integer courseId) {
        log.info("删除课程:{}", courseId);
        courseService.deleteCourse(courseId);
        return Result.success();
    }



    //排课管理

    /**
     * 排课列表
     * @param scheduleQueryDTO
     * @return
     */
    @GetMapping("/schedule/list")
    public Result<PageResult<CourseScheduleQueryVO>> getScheduleList(ScheduleQueryDTO scheduleQueryDTO) {
        log.info("排课列表:{}", scheduleQueryDTO);
        PageResult<CourseScheduleQueryVO> pageResult = courseService.getScheduleList(scheduleQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 排课冲突校验
     * @param payload
     * @return
     */
    @PostMapping("/schedule/checkConflict")
    public Result checkConflict(@RequestBody Object payload) {
        // 支持单条格式（ScheduleConflictDTO）和批量格式：{ items: [ {tempId, ...}, ... ] } 或直接数组
        try {
            if (payload instanceof List) {
                // 直接接收数组
                List<?> list = (List<?>) payload;
                List<ScheduleConflictDTO> items = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                for (Object o : list) {
                    ScheduleConflictDTO dto = mapper.convertValue(o, ScheduleConflictDTO.class);
                    items.add(dto);
                }
                List<BatchConflictResultDTO> results = courseService.conflictScheduleBatch(items);
                return Result.success(results);
            } else if (payload instanceof Map) {
                Map map = (Map) payload;
                if (map.containsKey("items")) {
                    Object itemsObj = map.get("items");
                    ObjectMapper mapper = new ObjectMapper();
                    List<ScheduleConflictDTO> items = mapper.convertValue(itemsObj,
                            mapper.getTypeFactory().constructCollectionType(List.class, ScheduleConflictDTO.class));
                    List<BatchConflictResultDTO> results = courseService.conflictScheduleBatch(items);
                    return Result.success(results);
                }
            }
        } catch (Exception ex) {
            log.warn("尝试批量解析失败，回退到单条解析: {}", ex.getMessage());
            // fallthrough
        }

        // 回退到单条行为，保持向后兼容：如果冲突则抛出异常（原有行为）
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        ScheduleConflictDTO dto = mapper.convertValue(payload, ScheduleConflictDTO.class);
        log.info("排课冲突校验:{}", dto);
        courseService.conflictSchedule(dto);
        return Result.success();
    }

    /**
     * 新增排课
     * @param scheduleSaveDTO
     * @return
     */
    @PostMapping("/schedule")
    public Result addSchedule( @RequestBody ScheduleSaveDTO scheduleSaveDTO) {
        log.info("新增排课:{}", scheduleSaveDTO);
        courseService.saveSchedule(scheduleSaveDTO);
        return Result.success();
    }

    /**
     * 编辑排课
     * @param scheduleSaveDTO
     * @return
     */
    @PutMapping("/schedule")
    public Result updateSchedule(@RequestBody ScheduleSaveDTO scheduleSaveDTO) {
        log.info("编辑排课:{}", scheduleSaveDTO);
        courseService.saveSchedule(scheduleSaveDTO);
        return Result.success();
    }

    /**
     * 删除排课(硬删除，前端不显示状态)
     * @param id
     * @return
     */
    @DeleteMapping("/schedule/{id}")
    public Result deleteSchedule(@PathVariable Integer id) {
        log.info("删除排课:{}",id);
        courseService.deleteSchedule(id);
        return  Result.success();
    }


    /**
     * 导出课表
     * @param response
     */
    @GetMapping("/schedule/export")
    public void exportSchedule(@RequestParam(required = false) Long courseId,
                               @RequestParam(required = false) String courseName,
                               HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("课表导出.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        if (response.isCommitted()) {
            log.warn("响应流已提交，终止导出操作");
            return;
        }

        try {
            //封装查询
            List<CourseScheduleExportVO> data = courseService.exportSchedule(courseId,courseName);

            if(data == null || data.isEmpty()){
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":400,\"msg\":\"暂无导出数据\"}");
                return;
            }

            //EasyExcel写出
            EasyExcel.write(response.getOutputStream(), CourseScheduleExportVO.class)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("课表数据")
                    .doWrite(data);
        }  catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            }
            log.error("课表导出失败，筛选条件：courseId={}, courseName={}", courseId, courseName, e);
        }
    }

}
