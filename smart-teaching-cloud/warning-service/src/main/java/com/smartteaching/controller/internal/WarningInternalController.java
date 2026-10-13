package com.smartteaching.controller.internal;

import com.smartteaching.api.dto.TruantWarningDTO;
import com.smartteaching.common.result.Result;
import com.smartteaching.service.warning.WarningService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学业预警内部接口（仅服务间 Feign 调用）
 * 考勤服务落定旷课后远程触发预警生成/刷新
 */
@RestController
@RequestMapping("/api/internal/warning")
public class WarningInternalController {

    @Resource
    private WarningService warningService;

    /**
     * 生成/刷新旷课预警（幂等 upsert）
     */
    @PostMapping("/truant")
    public Result<Void> createTruantWarning(@RequestBody TruantWarningDTO dto) {
        warningService.createTruantWarning(dto);
        return Result.success();
    }
}
