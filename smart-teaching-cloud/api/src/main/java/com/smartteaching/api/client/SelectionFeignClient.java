package com.smartteaching.api.client;

import com.smartteaching.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 选课状态 Feign 客户端
 * 指向 selection-score-service 的内部接口，供答疑等场景校验学生是否已选课
 */
@FeignClient(name = "selection-score-service", path = "/api/internal/selection")
public interface SelectionFeignClient {

    /**
     * 判断学生是否已选修某课程
     */
    @GetMapping("/hasSelected")
    Result<Boolean> hasSelected(@RequestParam("studentId") Long studentId,
                                @RequestParam("courseId") Long courseId);
}
