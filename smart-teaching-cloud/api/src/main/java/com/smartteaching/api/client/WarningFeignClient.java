package com.smartteaching.api.client;

import com.smartteaching.api.dto.TruantWarningDTO;
import com.smartteaching.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 学业预警 Feign 客户端
 * 指向 warning-service 的内部接口，考勤服务落定旷课后远程触发预警
 */
@FeignClient(name = "warning-service", path = "/api/internal/warning")
public interface WarningFeignClient {

    /**
     * 生成/刷新旷课预警（幂等 upsert）
     */
    @PostMapping("/truant")
    Result<Void> createTruantWarning(@RequestBody TruantWarningDTO dto);
}
