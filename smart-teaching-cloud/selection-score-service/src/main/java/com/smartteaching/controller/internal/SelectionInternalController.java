package com.smartteaching.controller.internal;

import com.smartteaching.common.result.Result;
import com.smartteaching.mapper.SelectionMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 选课状态内部接口（仅服务间 Feign 调用）
 * 供答疑等服务的 SelectionFeignClient 校验学生是否已选课
 */
@RestController
@RequestMapping("/api/internal/selection")
public class SelectionInternalController {

    @Resource
    private SelectionMapper selectionMapper;

    /**
     * 判断学生是否已选修某课程
     */
    @GetMapping("/hasSelected")
    public Result<Boolean> hasSelected(@RequestParam("studentId") Long studentId,
                                       @RequestParam("courseId") Long courseId) {
        return Result.success(selectionMapper.hasSelected(studentId, courseId));
    }
}
