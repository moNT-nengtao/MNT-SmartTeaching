package com.smartteaching.service.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.course.BatchConflictResultDTO;
import com.smartteaching.common.dto.course.CourseQueryDTO;
import com.smartteaching.common.dto.course.CourseSaveDTO;
import com.smartteaching.common.dto.schedule.ScheduleConflictDTO;
import com.smartteaching.common.dto.schedule.ScheduleQueryDTO;
import com.smartteaching.common.dto.schedule.ScheduleSaveDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.WeekUtil;
import com.smartteaching.common.vo.course.CourseQueryVO;
import com.smartteaching.common.vo.course.CourseScheduleExportVO;
import com.smartteaching.common.vo.course.CourseScheduleQueryVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.course.CourseSchedule;
import com.smartteaching.mapper.CourseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 课程列表
     * @return
     */
    @Override
    public PageResult<CourseQueryVO> getCourseList(CourseQueryDTO courseQueryDTO) {
        long pageNum = courseQueryDTO.getPageNum() == null ? 1 : courseQueryDTO.getPageNum();
        long pageSize = courseQueryDTO.getPageSize() == null ? 10 : courseQueryDTO.getPageSize();

        IPage<CourseQueryVO> iPage = new Page<>(pageNum, pageSize);
        IPage<CourseQueryVO> voIpage = courseMapper.selectCoursePage(iPage,courseQueryDTO);

        return PageResult.build(
                voIpage.getTotal(),
                voIpage.getPages(),
                voIpage.getCurrent(),
                voIpage.getSize(),
                voIpage.getRecords()
        );
    }

    /**
     * 新增课程
     *
     * @param courseSaveDTO
     * @return
     */
    @Override
    public void addCourse(CourseSaveDTO courseSaveDTO) {
        //校验重复
        LambdaQueryWrapper<Course> wrapper = Wrappers.lambdaQuery(Course.class);
        wrapper.eq(Course::getCode, courseSaveDTO.getCode());
        Long codeCount = courseMapper.selectCount(wrapper);
        if (codeCount > 0) {
            throw new BaseException(String.format(MessageConstant.COURSE_CODE_EXISTS, courseSaveDTO.getCode()));
        }
        //插入
        Course course = new Course();
        BeanUtils.copyProperties(courseSaveDTO, course);
        course.setStatus(1);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.insert(course);
    }

    /**
     * 编辑课程
     * @param courseSaveDTO
     */
    @Override
    public void updateCourse(CourseSaveDTO courseSaveDTO) {
        Course course = new Course();
        BeanUtils.copyProperties(courseSaveDTO, course);
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

    /**
     * 删除课程
     * @param courseId
     */
    @Override
    public void deleteCourse(Integer courseId) {
        if (courseMapper.selectById(courseId) == null) {
            throw new BaseException(MessageConstant.COURSE_NOT_EXIST);
        }
        LambdaUpdateWrapper <Course> wrapper = Wrappers.lambdaUpdate(Course.class);
        wrapper.eq(Course::getId, courseId)
                .set(Course::getStatus, 0)
        .set(Course::getUpdateTime, LocalDateTime.now());
        courseMapper.update(null, wrapper);
    }


    /**
     * 排课列表
     * @param scheduleQueryDTO
     * @return
     */
    @Override
    public PageResult<CourseScheduleQueryVO> getScheduleList(ScheduleQueryDTO scheduleQueryDTO) {
        long pageNum = scheduleQueryDTO.getPageNum() == null ? 1 : scheduleQueryDTO.getPageNum();
        long pageSize = scheduleQueryDTO.getPageSize() == null ? 10 : scheduleQueryDTO.getPageSize();

        IPage<CourseScheduleQueryVO> iPage = new Page<>(pageNum, pageSize);
        IPage<CourseScheduleQueryVO> voiPage = courseMapper.selectSchedulePage(iPage,scheduleQueryDTO);

        // 遍历转换 week 格式
        for (CourseScheduleQueryVO vo : voiPage.getRecords()) {
            vo.setWeek(WeekUtil.jsonToRangeStr(vo.getWeek()));
        }

        return PageResult.build(
                voiPage.getTotal(),
                voiPage.getPages(),
                voiPage.getCurrent(),
                voiPage.getSize(),
                voiPage.getRecords()
        );
    }

    /**
     * 新增排课
     * @param scheduleSaveDTO
     */
    @Override
    public void saveSchedule(ScheduleSaveDTO scheduleSaveDTO) {
        //检查数据库中同名课程的颜色
        LambdaQueryWrapper<CourseSchedule> wrapper = Wrappers.lambdaQuery(CourseSchedule.class);
        wrapper.eq(CourseSchedule::getCourseId, scheduleSaveDTO.getCourseId());
        String getColor = courseMapper.selectColor(scheduleSaveDTO);
        if (getColor == null) {
            getColor = getNextColor();
        }

        //week转换格式
        String week = WeekUtil.rangeStrToJson(scheduleSaveDTO.getWeek());

        CourseSchedule saveSchedule = new CourseSchedule();
        BeanUtils.copyProperties(scheduleSaveDTO, saveSchedule);
        saveSchedule.setWeek(week);
        saveSchedule.setColor(getColor);

        if (scheduleSaveDTO.getId() == null) {
            courseMapper.addSchedule(saveSchedule);
        }else {
            courseMapper.updateSchedule(saveSchedule);
        }


    }

    /**
     * 排课冲突校验(单)
     * @param scheduleConflictDTO
     */
    @Override
    public void conflictSchedule(ScheduleConflictDTO scheduleConflictDTO) {
        log.info("排课冲突检查");
        //week范围字符转json
        String week = WeekUtil.rangeStrToJson(scheduleConflictDTO.getWeek());
        scheduleConflictDTO.setWeek(week);

        //查找相同day+lesson下相同teacher\room\class
        List<CourseSchedule> scheduleList = courseMapper.selectConflictSchedules(scheduleConflictDTO);

        //工具类查week的重复
        for (CourseSchedule courseSchedule : scheduleList) {
            if (WeekUtil.hasIntersection(courseSchedule.getWeek(), scheduleConflictDTO.getWeek())) {
                throw new BaseException(MessageConstant.SCHEDULE_CONFLICT);
            }
        }
    }

    /**
     * 排课冲突校验(批量)
     * @param scheduleConflictDTOList
     * @return
     */
    @Override
    public java.util.List<BatchConflictResultDTO> conflictScheduleBatch(java.util.List<ScheduleConflictDTO> scheduleConflictDTOList) {
        java.util.List<BatchConflictResultDTO> results = new java.util.ArrayList<>();
        if (scheduleConflictDTOList == null || scheduleConflictDTOList.isEmpty()) return results;

        // 1. 转换 week 格式
        for (ScheduleConflictDTO dto : scheduleConflictDTOList) {
            String weekJson = WeekUtil.rangeStrToJson(dto.getWeek());
            dto.setWeek(weekJson);
        }

        // 2. 先检测所有项与数据库的冲突（保留原逻辑）
        for (ScheduleConflictDTO dto : scheduleConflictDTOList) {
            BatchConflictResultDTO r = new BatchConflictResultDTO();
            r.setTempId(dto.getTempId());

            List<CourseSchedule> scheduleList = courseMapper.selectConflictSchedules(dto);
            boolean conflictFound = false;
            for (CourseSchedule cs : scheduleList) {
                if (WeekUtil.hasIntersection(cs.getWeek(), dto.getWeek())) {
                    r.setConflict(MessageConstant.SCHEDULE_CONFLICT_DESC);
                    conflictFound = true;
                    break;
                }
            }

            if (!conflictFound) {
                r.setConflict(null);
            }
            results.add(r);
        }

        // 3. 新增：批量内两两冲突检测
        for (int i = 0; i < scheduleConflictDTOList.size(); i++) {
            ScheduleConflictDTO a = scheduleConflictDTOList.get(i);
            for (int j = i + 1; j < scheduleConflictDTOList.size(); j++) {
                ScheduleConflictDTO b = scheduleConflictDTOList.get(j);

                // 检查是否为同一节次（同日同节）
                if (!a.getDay().equals(b.getDay()) || !a.getLesson().equals(b.getLesson())) {
                    continue;
                }

                // 检查周次是否有交集
                if (!WeekUtil.hasIntersection(a.getWeek(), b.getWeek())) {
                    continue;
                }

                // 检查教师冲突
                if (a.getTeacherId() != null && a.getTeacherId().equals(b.getTeacherId())) {
                    setConflictForResult(results, a.getTempId(), MessageConstant.CONFLICT_BATCH_TEACHER);
                    setConflictForResult(results, b.getTempId(), MessageConstant.CONFLICT_BATCH_TEACHER);
                    continue;
                }

                // 检查班级冲突
                if (a.getClassId() != null && a.getClassId().equals(b.getClassId())) {
                    setConflictForResult(results, a.getTempId(), MessageConstant.CONFLICT_BATCH_CLASS);
                    setConflictForResult(results, b.getTempId(), MessageConstant.CONFLICT_BATCH_CLASS);
                    continue;
                }

                // 检查教室冲突
                if (a.getRoom() != null && b.getRoom() != null && a.getRoom().equals(b.getRoom())) {
                    setConflictForResult(results, a.getTempId(), MessageConstant.CONFLICT_BATCH_ROOM);
                    setConflictForResult(results, b.getTempId(), MessageConstant.CONFLICT_BATCH_ROOM);
                }
            }
        }

        return results;
    }

    /**
     * 删除排课
     * @param id
     */
    @Override
    public void deleteSchedule(Integer id) {
        //检查是否存在
        CourseSchedule schedule = courseMapper.selectScheduleById(id);
        if (schedule == null) {
            throw new BaseException(MessageConstant.SCHEDULE_NOT_EXIST);
        }

        courseMapper.deleteScheduleById(id);
    }

    /**
     * 导出课表
     * @return
     */
    @Override
    public List<CourseScheduleExportVO> exportSchedule(Long courseId, String courseName) {
        log.info("导出课表service...");

        List<CourseScheduleExportVO> result = courseMapper.selectExportList(courseId,courseName);
        //数据转换
        String[] weekDayArr = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        Map<Integer, String> lessonMap = Map.of(
                1, "第1节",
                2, "第2节",
                3, "第3节",
                4, "第4节",
                5, "第5节",
                6, "第6节"
        );
        List<CourseScheduleExportVO> exportList = result.stream().map(item -> {
            // 数字day转为中文星期文本，给Excel导出
            if (item.getDay() != null && item.getDay() >= 1 && item.getDay() <=7) {
                item.setWeekdayText(weekDayArr[item.getDay()]);
            } else {
                item.setWeekdayText("");
            }

            // 数字lesson转为中文节次
            if (item.getLesson() != null) {
                item.setLessonText(lessonMap.getOrDefault(item.getLesson(), ""));
            } else {
                item.setLessonText("");
            }

            String weekDisplay = WeekUtil.jsonToRangeStr(item.getWeekJson());
            item.setWeek(weekDisplay);

            return item;
        }).collect(Collectors.toList());

        return exportList;
    }

    /**
     * 获取颜色（轮询）
     */
    private static final String[] PRESET_COLORS = {
            "#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399",
            "#8E44AD", "#1ABC9C", "#E74C3C", "#2ECC71", "#F39C12",
            "#3498DB", "#9B59B6", "#1A1A2E", "#E94560", "#0F3460", "#533483"
    };
    private static int colorIndex = 0;
    private String getNextColor() {
        String color = PRESET_COLORS[colorIndex % PRESET_COLORS.length];
        colorIndex++;
        return color;
    }


    /**
     * 辅助方法：为指定 tempId 设置冲突信息（保留第一条冲突信息）
     */
    private void setConflictForResult(java.util.List<BatchConflictResultDTO> results, String tempId, String conflictMsg) {
        for (BatchConflictResultDTO r : results) {
            if (r.getTempId().equals(tempId) && r.getConflict() == null) {
                r.setConflict(conflictMsg);
                break;
            }
        }
    }

}

