package com.smartteaching.service.warning;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.warning.WarningQueryDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.SecurityUtils;
import com.smartteaching.common.vo.warning.WarningDetailVO;
import com.smartteaching.common.vo.warning.WarningExportVO;
import com.smartteaching.common.vo.warning.WarningListVO;
import com.smartteaching.common.vo.warning.WarningStatsVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.entity.warning.Warning;
import com.smartteaching.mapper.UserMapper;
import com.smartteaching.mapper.WarningMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName WarningServiceImpl
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:39
 **/
@Service
@Slf4j
public class WarningServiceImpl implements WarningService {

    @Resource
    private WarningMapper warningMapper;

    @Resource
    private UserMapper userMapper;
    //日期格式化
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 预警列表
     * @param warningQueryDTO
     * @return
     */
    @Override
    public PageResult<WarningListVO> getWarningList(WarningQueryDTO warningQueryDTO) {
        long pageNum = warningQueryDTO.getPageNum() == null ? 1 : warningQueryDTO.getPageNum();
        long pageSize = warningQueryDTO.getPageSize() == null ? 10 : warningQueryDTO.getPageSize();

        // 权限控制：学生只看自己的预警，教师/管理员看全部
        Long currentUserId = getCurrentUserIdForFilter();

        IPage<WarningListVO> iPage = new Page<>(pageNum, pageSize);
        IPage<WarningListVO> voIPage = warningMapper.selectWarningPage(iPage, warningQueryDTO, currentUserId);

        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 预警详情
     * @param id
     * @return
     */
    @Override
    public WarningDetailVO getWarningDetail(Long id) {
        log.info("获取预警详情, id: {}", id);

        // 权限控制：学生只能查看自己的预警详情
        checkDetailPermission(id);

        WarningDetailVO detail = warningMapper.selectWarningDetail(id);
        if (detail == null) {
            throw new BaseException(MessageConstant.WARNING_NOT_EXIST);
        }

        // 查询成绩趋势数据
        List<WarningDetailVO.ScoreTrendData> scoreTrend = warningMapper.selectScoreTrend(id);
        detail.setScoreTrend(scoreTrend);

        // 查询考勤数据
        WarningDetailVO.AttendanceData attendance = warningMapper.selectAttendanceData(id);
        detail.setAttendance(attendance);

        // 生成改进建议
        List<String> suggestions = generateSuggestions(detail);
        detail.setSuggestions(suggestions);

        return detail;
    }

    /**
     * 预警统计
     * @return
     */
    @Override
    public WarningStatsVO getWarningStats() {
        log.info("获取预警统计数据");
        return warningMapper.selectWarningStats();
    }

    /**
     * 导出预警报告（返回导出VO列表）
     *
     * @param warningQueryDTO
     * @return
     */
    @Override
    public List<WarningExportVO> exportWarningReport(WarningQueryDTO warningQueryDTO) {
        log.info("导出预警报告");
        // 权限控制：学生只导出自己的预警
        Long currentUserId = getCurrentUserIdForFilter();
        List<WarningListVO> list = warningMapper.selectWarningExportList(warningQueryDTO, currentUserId);

        List<WarningExportVO> exportList = new ArrayList<>();
        for (WarningListVO item : list) {
            WarningExportVO exportVO = new WarningExportVO();
            exportVO.setStudentName(item.getStudentName());
            exportVO.setStudentNo(item.getStudentNo());
            exportVO.setClassName(item.getClassName());
            exportVO.setLevelText(getLevelText(item.getLevel()));
            exportVO.setTypeText(getTypeText(item.getWarningType()));
            exportVO.setReason(item.getReason());
            exportVO.setCreateTime(item.getCreateTime() != null ?
                    item.getCreateTime().format(DATE_TIME_FORMATTER) : "");
            exportVO.setStatusText(item.getStatus() != null && item.getStatus() == 2 ? MessageConstant.WARNING_STATUS_HANDLED : MessageConstant.WARNING_STATUS_UNHANDLED);
            exportList.add(exportVO);
        }
        return exportList;
    }

    /**
     * 获取预警等级文本
     */
    public static String getLevelText(Integer level) {
        if (level == null) return "";
        switch (level) {
            case 3: return MessageConstant.WARNING_LEVEL_SEVERE;
            case 2: return MessageConstant.WARNING_LEVEL_MEDIUM;
            case 1: return MessageConstant.WARNING_LEVEL_SLIGHT;
            default: return "";
        }
    }

    /**
     * 获取预警类型文本
     */
    public static String getTypeText(String type) {
        if (type == null) return "";
        switch (type) {
            case "absent": return MessageConstant.WARNING_TYPE_ABSENT;
            case "score": return MessageConstant.WARNING_TYPE_SCORE;
            case "homework": return MessageConstant.WARNING_TYPE_HOMEWORK;
            default: return type;
        }
    }

    /**
     * 根据预警信息生成改进建议
     */
    private List<String> generateSuggestions(WarningDetailVO detail) {
        List<String> suggestions = new ArrayList<>();

        if (detail == null) {
            return suggestions;
        }

        String level = detail.getLevel();
        String warningType = detail.getWarningType();

        // 根据预警等级生成建议
        if ("high".equals(level)) {
            suggestions.add(MessageConstant.SUGGEST_HIGH_1);
            suggestions.add(MessageConstant.SUGGEST_HIGH_2);
        } else if ("medium".equals(level)) {
            suggestions.add(MessageConstant.SUGGEST_MEDIUM_1);
            suggestions.add(MessageConstant.SUGGEST_MEDIUM_2);
        } else if ("low".equals(level)) {
            suggestions.add(MessageConstant.SUGGEST_LOW_1);
            suggestions.add(MessageConstant.SUGGEST_LOW_2);
        }

        // 根据预警类型生成专项建议
        if ("score".equals(warningType)) {
            suggestions.add(MessageConstant.SUGGEST_SCORE_1);
            suggestions.add(MessageConstant.SUGGEST_SCORE_2);
        } else if ("absent".equals(warningType)) {
            suggestions.add(MessageConstant.SUGGEST_ABSENT_1);
            suggestions.add(MessageConstant.SUGGEST_ABSENT_2);
        } else if ("homework".equals(warningType)) {
            suggestions.add(MessageConstant.SUGGEST_HOMEWORK_1);
            suggestions.add(MessageConstant.SUGGEST_HOMEWORK_2);
        }

        return suggestions;
    }

    /**
     * 获取当前用户ID用于列表过滤：学生返回自己的ID，教师/管理员返回null（看全部）
     */
    private Long getCurrentUserIdForFilter() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return null;
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            return null;
        }
        if ("student".equals(user.getRole())) {
            return user.getId();
        }
        return null;
    }

    /**
     * 校验预警详情权限：学生只能查看自己的预警
     */
    private void checkDetailPermission(Long warningId) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return;
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null || !"student".equals(user.getRole())) {
            return;
        }
        Warning warning = warningMapper.selectById(warningId);
        if (warning == null || !warning.getUserId().equals(user.getId())) {
            throw new BaseException("无权查看该预警详情");
        }
    }
}
