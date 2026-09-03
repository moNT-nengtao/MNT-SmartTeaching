package com.smartteaching.controller.homework;

import com.smartteaching.common.dto.homework.HomeworkGradeDTO;
import com.smartteaching.common.dto.homework.HomeworkQueryDTO;
import com.smartteaching.common.dto.homework.HomeworkSaveDTO;
import com.smartteaching.common.dto.homework.HomeworkSubmitDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.homework.HomeworkListVO;
import com.smartteaching.common.vo.homework.HomeworkStatsVO;
import com.smartteaching.common.vo.homework.HomeworkStudentListVO;
import com.smartteaching.common.vo.homework.HomeworkSubmissionVO;
import com.smartteaching.entity.homework.Homework;
import com.smartteaching.service.homework.HomeworkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName HomeworkController
 * @Description 作业管理控制器
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@RestController
@RequestMapping("/api/homework")
@Slf4j
public class HomeworkController {

    @Resource
    private HomeworkService homeworkService;

    /**
     * 发布作业（教师）
     * 普通字段走 FormData，附件文件单独 append `file`
     */
    @PostMapping
    public Result publishHomework(HomeworkSaveDTO dto,
                                  @RequestPart(required = false) MultipartFile file) {
        log.info("发布作业: {}", dto);
        homeworkService.publishHomework(dto, file);
        return Result.success("发布作业成功");
    }

    /**
     * 编辑作业（教师/管理员）
     */
    @PutMapping
    public Result updateHomework(HomeworkSaveDTO dto,
                                 @RequestPart(required = false) MultipartFile file) {
        log.info("编辑作业: {}", dto);
        homeworkService.updateHomework(dto, file);
        return Result.success("编辑作业成功");
    }

    /**
     * 删除作业（教师/管理员，软删除）
     */
    @DeleteMapping("/{id}")
    public Result deleteHomework(@PathVariable Long id) {
        log.info("删除作业: {}", id);
        homeworkService.deleteHomework(id);
        return Result.success("删除作业成功");
    }

    /**
     * 教师/管理员 作业列表
     */
    @GetMapping("/list")
    public Result<PageResult<HomeworkListVO>> getHomeworkList(HomeworkQueryDTO queryDTO) {
        log.info("作业列表: {}", queryDTO);
        PageResult<HomeworkListVO> page = homeworkService.getHomeworkList(queryDTO);
        return Result.success(page);
    }

    /**
     * 学生 我的作业列表（含提交状态和成绩）
     */
    @GetMapping("/my")
    public Result<PageResult<HomeworkStudentListVO>> getMyHomeworkList(HomeworkQueryDTO queryDTO) {
        log.info("学生作业列表: {}", queryDTO);
        PageResult<HomeworkStudentListVO> page = homeworkService.getStudentHomeworkList(queryDTO);
        return Result.success(page);
    }

    /**
     * 作业详情
     */
    @GetMapping("/{id}")
    public Result<Homework> getHomeworkDetail(@PathVariable Long id) {
        log.info("作业详情: {}", id);
        Homework homework = homeworkService.getHomeworkDetail(id);
        return Result.success(homework);
    }

    /**
     * 查看某作业的提交列表（教师/管理员）
     */
    @GetMapping("/{id}/submissions")
    public Result<List<HomeworkSubmissionVO>> getSubmissionList(@PathVariable("id") Long homeworkId) {
        log.info("作业提交列表: homeworkId={}", homeworkId);
        List<HomeworkSubmissionVO> list = homeworkService.getSubmissionList(homeworkId);
        return Result.success(list);
    }

    /**
     * 学生提交作业
     */
    @PostMapping("/submit")
    public Result submitHomework(HomeworkSubmitDTO dto,
                                 @RequestPart(required = false) MultipartFile file) {
        log.info("提交作业: {}", dto);
        homeworkService.submitHomework(dto, file);
        return Result.success("提交作业成功");
    }

    /**
     * 教师批改作业
     */
    @PutMapping("/grade")
    public Result gradeHomework(@RequestBody HomeworkGradeDTO dto) {
        log.info("批改作业: {}", dto);
        homeworkService.gradeHomework(dto);
        return Result.success("批改成功");
    }

    /**
     * 管理员作业统计
     */
    @GetMapping("/stats")
    public Result<HomeworkStatsVO> getStats() {
        log.info("作业统计");
        HomeworkStatsVO stats = homeworkService.getStats();
        return Result.success(stats);
    }
}
