package com.smartteaching.service.homework;

import com.smartteaching.common.dto.homework.HomeworkGradeDTO;
import com.smartteaching.common.dto.homework.HomeworkQueryDTO;
import com.smartteaching.common.dto.homework.HomeworkSaveDTO;
import com.smartteaching.common.dto.homework.HomeworkSubmitDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.homework.HomeworkListVO;
import com.smartteaching.common.vo.homework.HomeworkStatsVO;
import com.smartteaching.common.vo.homework.HomeworkStudentListVO;
import com.smartteaching.common.vo.homework.HomeworkSubmissionVO;
import com.smartteaching.entity.homework.Homework;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName HomeworkService
 * @Description 作业 Service 接口
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
public interface HomeworkService {

    /**
     * 发布作业
     */
    void publishHomework(HomeworkSaveDTO dto, MultipartFile file);

    /**
     * 编辑作业
     */
    void updateHomework(HomeworkSaveDTO dto, MultipartFile file);

    /**
     * 删除作业
     */
    void deleteHomework(Long id);

    /**
     * 教师/管理员 作业列表
     */
    PageResult<HomeworkListVO> getHomeworkList(HomeworkQueryDTO queryDTO);

    /**
     * 学生 作业列表（含提交状态和成绩）
     */
    PageResult<HomeworkStudentListVO> getStudentHomeworkList(HomeworkQueryDTO queryDTO);

    /**
     * 作业详情
     */
    Homework getHomeworkDetail(Long id);

    /**
     * 查看某作业的提交列表
     */
    List<HomeworkSubmissionVO> getSubmissionList(Long homeworkId);

    /**
     * 学生提交作业
     */
    void submitHomework(HomeworkSubmitDTO dto, MultipartFile file);

    /**
     * 教师批改作业
     */
    void gradeHomework(HomeworkGradeDTO dto);

    /**
     * 管理员作业统计
     */
    HomeworkStatsVO getStats();
}
