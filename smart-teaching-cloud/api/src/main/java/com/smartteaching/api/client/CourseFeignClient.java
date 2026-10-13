package com.smartteaching.api.client;

import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 课程信息 Feign 客户端
 * 指向 course-service 的内部接口，供考勤/成绩/选课/评价/答疑等服务查询课程
 */
@FeignClient(name = "course-service", path = "/api/internal/course")
public interface CourseFeignClient {

    /**
     * 按ID查询课程
     */
    @GetMapping("/{id}")
    Result<CourseInfoVO> getCourseById(@PathVariable("id") Long id);

    /**
     * 按状态查询课程列表（status 为空则查询全部）
     */
    @GetMapping("/list")
    Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status,
                                           @RequestParam(value = "teacherId", required = false) Long teacherId);
}
