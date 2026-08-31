package com.smartteaching.service.warning;

import com.smartteaching.common.dto.warning.WarningQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.warning.WarningDetailVO;
import com.smartteaching.common.vo.warning.WarningExportVO;
import com.smartteaching.common.vo.warning.WarningListVO;
import com.smartteaching.common.vo.warning.WarningStatsVO;

import java.util.List;

/**
 * @ClassName WarningService
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:38
 **/
public interface WarningService {

    /**
     * 预警列表
     * @param queryDTO
     * @return
     */
    PageResult<WarningListVO> getWarningList(WarningQueryDTO queryDTO);

    /**
     * 预警详情
     * @param id
     * @return
     */
    WarningDetailVO getWarningDetail(Long id);

    /**
     * 预警统计
     * @return
     */
    WarningStatsVO getWarningStats();

    /**
     * 导出预警报告
     *
     * @param warningQueryDTO
     * @return
     */
    List<WarningExportVO> exportWarningReport(WarningQueryDTO warningQueryDTO);
}
