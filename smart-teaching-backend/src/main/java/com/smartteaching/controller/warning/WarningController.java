package com.smartteaching.controller.warning;

import com.alibaba.excel.EasyExcel;
import com.smartteaching.common.dto.warning.WarningQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.warning.WarningDetailVO;
import com.smartteaching.common.vo.warning.WarningExportVO;
import com.smartteaching.common.vo.warning.WarningListVO;
import com.smartteaching.common.vo.warning.WarningStatsVO;
import com.smartteaching.service.warning.WarningService;
import com.smartteaching.service.warning.WarningServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @ClassName WarningController
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:37
 **/
@RestController
@RequestMapping("/api/warning")
@Slf4j
public class WarningController {

    @Resource
    private WarningService warningService;

    /**
     * 预警列表
     * @param queryDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult<WarningListVO>> getWarningList(WarningQueryDTO queryDTO) {
        log.info("预警列表: {}", queryDTO);
        PageResult<WarningListVO> page = warningService.getWarningList(queryDTO);
        return Result.success(page);
    }

    /**
     * 预警详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<WarningDetailVO> getWarningDetail(@PathVariable Long id) {
        log.info("预警详情:{}",id);
        WarningDetailVO detail = warningService.getWarningDetail(id);
        if (detail == null) {
            return Result.error("预警记录不存在");
        }
        return Result.success(detail);
    }

    /**
     * 预警统计
     * @return
     */
    @GetMapping("/stats")
    public Result<WarningStatsVO> getWarningStats() {
        WarningStatsVO stats = warningService.getWarningStats();
        return Result.success(stats);
    }

    /**
     * 导出预警报告
     * @param queryDTO
     * @param response
     * @throws IOException
     */
    @GetMapping("/export")
    public void exportWarningReport(WarningQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("学业预警报告_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx",
                StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        if (response.isCommitted()) {
            log.warn("响应流已提交，终止导出操作");
            return;
        }

        try {
            // 查询导出数据
            List<WarningExportVO> data = warningService.exportWarningReport(queryDTO);

            if (data == null || data.isEmpty()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":400,\"msg\":\"暂无导出数据\"}");
                return;
            }

            // EasyExcel写入
            EasyExcel.write(response.getOutputStream(), WarningExportVO.class)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("学业预警报告")
                    .doWrite(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            }
            log.error("预警报告导出失败，筛选条件：{}", queryDTO, e);
        }
    }

}
