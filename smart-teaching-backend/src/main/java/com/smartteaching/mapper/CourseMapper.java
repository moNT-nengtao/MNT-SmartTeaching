package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.course.CourseQueryDTO;
import com.smartteaching.common.dto.schedule.ScheduleConflictDTO;
import com.smartteaching.common.dto.schedule.ScheduleQueryDTO;
import com.smartteaching.common.dto.schedule.ScheduleSaveDTO;
import com.smartteaching.common.vo.course.CourseQueryVO;
import com.smartteaching.common.vo.course.CourseScheduleExportVO;
import com.smartteaching.common.vo.course.CourseScheduleQueryVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.course.CourseSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 课程列表
     *
     * @param iPage
     * @param courseQueryDTO
     * @return
     */
    IPage<CourseQueryVO> selectCoursePage(@Param("iPage") IPage<CourseQueryVO> iPage,
                                          @Param("courseQueryDTO") CourseQueryDTO courseQueryDTO);

    /**
     * 排课列表
     * @param iPage
     * @param scheduleQueryDTO
     * @return
     */
    IPage<CourseScheduleQueryVO> selectSchedulePage(@Param("iPage") IPage<CourseScheduleQueryVO> iPage,
                                                    @Param("scheduleQueryDTO") ScheduleQueryDTO scheduleQueryDTO);


    /**
     * 排课冲突校验
     * @param scheduleConflictDTO
     * @return
     */
    List<CourseSchedule> selectConflictSchedules(ScheduleConflictDTO scheduleConflictDTO);

    /**
     * 获取同名课程颜色
     * @param scheduleSaveDTO
     * @return
     */
    @Select("select color from course_schedule where course_id = #{courseId} LIMIT 1")
    String selectColor(ScheduleSaveDTO scheduleSaveDTO);

    /**
     * 新增排课
     * @param addSchedule
     */
    void addSchedule(CourseSchedule addSchedule);

    /**
     * 编辑排课
     * @param saveSchedule
     */
    void updateSchedule(CourseSchedule saveSchedule);

    /**
     * 根据id查询Schedule
     * @param id
     * @return
     */
    @Select("select * from course_schedule where id = #{id}")
    CourseSchedule selectScheduleById(Integer id);

    /**
     * 删除排课
     * @param id
     */
    @Delete("delete from course_schedule where id = #{id}")
    void deleteScheduleById(Integer id);

    /**
     * 导出课表
     * @param courseId
     * @param courseName
     * @return
     */
    List<CourseScheduleExportVO> selectExportList(Long courseId, String courseName);
}
