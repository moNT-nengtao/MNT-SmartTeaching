package com.smartteaching.controller.internal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.common.result.Result;
import com.smartteaching.entity.course.Course;
import com.smartteaching.mapper.CourseMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程信息内部接口（仅服务间 Feign 调用）
 * 供考勤/成绩/选课/评价/答疑等服务的 CourseFeignClient 使用
 */
@RestController
@RequestMapping("/api/internal/course")
public class CourseInternalController {

    @Resource
    private CourseMapper courseMapper;

    /**
     * 按ID查询课程基础信息
     */
    @GetMapping("/{id}")
    public Result<CourseInfoVO> getCourseById(@PathVariable("id") Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        return Result.success(toVO(course));
    }

    /**
     * 按状态查询课程列表（status 为空查询全部，1启用 0停用）
     */
    @GetMapping("/list")
    public Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "teacherId", required = false) Long teacherId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        wrapper.orderByDesc(Course::getId);
        List<Course> courses = courseMapper.selectList(wrapper);
        List<CourseInfoVO> list = new ArrayList<>();
        if (courses != null) {
            for (Course course : courses) {
                list.add(toVO(course));
            }
        }
        return Result.success(list);
    }

    private CourseInfoVO toVO(Course course) {
        CourseInfoVO vo = new CourseInfoVO();
        BeanUtils.copyProperties(course, vo);
        return vo;
    }
}
