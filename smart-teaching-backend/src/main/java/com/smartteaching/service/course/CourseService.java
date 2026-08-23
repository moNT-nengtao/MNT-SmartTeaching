package com.smartteaching.service.course;

import com.smartteaching.common.dto.*;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.CourseQueryVO;
import com.smartteaching.common.vo.CourseScheduleExportVO;
import com.smartteaching.common.vo.ScheduleQueryVO;

import java.util.List;

public interface CourseService {
    /**
     * 课程列表
     * @return
     */
    PageResult<CourseQueryVO> getCourseList(CourseQueryDTO courseQueryDTO);

    /**
     * 新增课程
     *
     * @param courseSaveDTO
     * @return
     */
    void addCourse(CourseSaveDTO courseSaveDTO);

    /**
     * 编辑课程
     * @param courseSaveDTO
     */
    void updateCourse(CourseSaveDTO courseSaveDTO);

    /**
     * 删除课程
     * @param courseId
     */
    void deleteCourse(Integer courseId);

    /**
     * 排课列表
     * @param scheduleQueryDTO
     * @return
     */
    PageResult<ScheduleQueryVO> getScheduleList(ScheduleQueryDTO scheduleQueryDTO);

    /**
     * 新增AND编辑-排课
     * @param scheduleSaveDTO
     */
    void saveSchedule(ScheduleSaveDTO scheduleSaveDTO);

    /**
     * 排课冲突校验（单条）
     * @param scheduleConflictDTO
     */
    void conflictSchedule(ScheduleConflictDTO scheduleConflictDTO);

    /**
     * 排课冲突批量校验（返回每条的冲突信息，不写入数据库）
     * @param scheduleConflictDTOList
     */
    java.util.List<com.smartteaching.common.dto.BatchConflictResultDTO> conflictScheduleBatch(java.util.List<ScheduleConflictDTO> scheduleConflictDTOList);


    /**
     * 删除排课
     * @param id
     */
    void deleteSchedule(Integer id);

    /**
     * 导出课表
     * @return
     */
    List<CourseScheduleExportVO> exportSchedule(Long courseId, String courseName);
}
