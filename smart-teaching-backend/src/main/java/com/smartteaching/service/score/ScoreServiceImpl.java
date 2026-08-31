package com.smartteaching.service.score;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.score.ScoreEnterDTO;
import com.smartteaching.common.dto.score.ScorePageReqDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.RedisUtils;
import com.smartteaching.common.vo.score.*;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.score.Score;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.ScoreMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @ClassName ScoreServiceImpl
 * @Description 成绩服务实现类
 * @Author MNT
 * @Date 2026/8/27 11:17
 **/
@Service
@Slf4j
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    @Resource
    private CourseMapper courseMapper;
    @Resource
    private ScoreMapper scoreMapper;
    @Resource
    private RedisUtils redisUtils;
    private static final long DASHBOARD_CACHE_MINUTE = 20;
    private static final String CACHE_PREFIX_SCORESTATS = "dashboard:scorestats";

    /**
     * 成绩统计缓存
     * @param dto
     * @return
     */
    public ScoreStatsQueryVO getScoreStats(ScorePageReqDTO dto) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_SCORESTATS);

        // 查缓存
        String cachedJson = redisUtils.getStr(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, ScoreStatsQueryVO.class);
        }

        // 缓存未命中，查询数据库组装数据
        ScoreStatsQueryVO vo = buildScoreDashboardData(dto);

        // 3. 写入缓存（JSON字符串形式）
        String json = JSON.toJSONString(vo);
        redisUtils.setStr(cacheKey, json, DASHBOARD_CACHE_MINUTE, TimeUnit.MINUTES);

        return vo;
    }

    /**
     * 成绩统计数据
     * @param dto
     * @return
     */
    public ScoreStatsQueryVO buildScoreDashboardData(ScorePageReqDTO dto) {
        ScoreStatsQueryVO vo = new ScoreStatsQueryVO();

        //1.统计卡片
        ScoreStatsQueryVO.StatCardData statCard = baseMapper.selectStatCard(dto);
        if (statCard != null) {
            if (statCard.getTotalRecord() != null && statCard.getTotalRecord() > 0) {
                BigDecimal passRate = new BigDecimal(statCard.getPassStudent())
                        .divide(new BigDecimal(statCard.getTotalRecord()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                statCard.setPassRate(passRate);
            }
            vo.setStatCards(statCard);
        }

        //2.饼图
        ScoreStatsQueryVO.ScoreDistribution pie = baseMapper.selectScoreDistribution(dto);
        if (pie != null) {
            List<ScoreStatsQueryVO.ChartItem> chartData = new ArrayList<>();
            addItem(chartData, MessageConstant.SCORE_EXCELLENT, pie.getExcellentCount());
            addItem(chartData, MessageConstant.SCORE_GOOD, pie.getGoodCount());
            addItem(chartData, MessageConstant.SCORE_MEDIUM, pie.getMediumCount());
            addItem(chartData, MessageConstant.SCORE_PASS, pie.getPassCount());
            addItem(chartData, MessageConstant.SCORE_FAIL, pie.getFailCount());
            pie.setChartData(chartData);
            vo.setPieData(pie);
        }

        //3.学院平均分柱状图
        List<ScoreStatsQueryVO.CollegeScoreCompare.Item> collegeItems = baseMapper.selectCollegeAvgScore(dto);
        ScoreStatsQueryVO.CollegeScoreCompare bar = new ScoreStatsQueryVO.CollegeScoreCompare();
        List<String> collegeNameList = new ArrayList<>();
        List<BigDecimal> avgScoreList = new ArrayList<>();
        List<Long> countList = new ArrayList<>();
        for (ScoreStatsQueryVO.CollegeScoreCompare.Item item : collegeItems) {
            collegeNameList.add(item.getColleges());
            avgScoreList.add(item.getAvgScores() != null ? item.getAvgScores().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            countList.add(item.getStudentCounts());
        }
        bar.setColleges(collegeNameList);
        bar.setAvgScores(avgScoreList);
        bar.setStudentCounts(countList);
        vo.setBarData(bar);
        return vo;
    }

    private void addItem(List<ScoreStatsQueryVO.ChartItem> list, String name, Long value) {
        ScoreStatsQueryVO.ChartItem item = new ScoreStatsQueryVO.ChartItem();
        item.setName(name);
        item.setValue(value == null ? 0L : value);
        list.add(item);
    }

    /**
     * 异常成绩列表
     * @param dto
     * @return
     */
    @Override
    public PageResult<ScoreStatsQueryVO.AbnormalScore> getAbnormalScores(ScorePageReqDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        Page<ScoreStatsQueryVO.AbnormalScore> page = new Page<>(pageNum, pageSize);
        IPage<ScoreStatsQueryVO.AbnormalScore> voIPage = baseMapper.selectAbnormalScorePage(page, dto);

        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 导出异常成绩
     * @param dto
     * @return
     */
    @Override
    public List<ScoreExportVO> exportAbnormalScore(ScorePageReqDTO dto) {
        List<ScoreExportVO> voList = scoreMapper.selectExportList(dto);
        log.info("导出查询结果：共{}条数据，内容：{}", voList.size(), voList);
        return voList;
    }

    /**
     * 我的成绩
     * @param dto
     * @param studentId
     * @return
     */
    @Override
    public List<ScoreMyQueryVO> scoryMyQueryPage(ScorePageReqDTO dto, Long studentId) {
        List<ScoreMyQueryVO> voList = scoreMapper.selectMyScore(dto, studentId);
        return voList;
    }

    /**
     * 获取课程成绩列表（教师录入用）
     * @param courseId
     * @return
     */
    @Override
    public List<ScoreCourseQueryVO> getCourseScoreList(Long courseId) {
        List<ScoreCourseQueryVO> voList = scoreMapper.selectCourseScoreList(courseId);
        return voList;
    }

    /**
     * 录入成绩
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enterScore(ScoreEnterDTO dto) {
        Long courseId = dto.getCourseId();
        List<ScoreEnterDTO.StudentScore> scoreList = dto.getScores();

        log.info("开始录入成绩: courseId={}, 学生数量={}", courseId, scoreList.size());

        // 获取教师ID
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new RuntimeException("课程不存在或已停用");
        }

        Long teacherId = course.getTeacherId();
        if (teacherId == null) {
            throw new RuntimeException("课程未分配教师");
        }

        // 构建实体列表
        List<Score> scores = new ArrayList<>();
        for (ScoreEnterDTO.StudentScore dtoScore : scoreList) {
            Score entity = new Score();
            BeanUtils.copyProperties(dtoScore, entity);
            entity.setCourseId(courseId);
            entity.setTeacherId(teacherId);
            entity.setScore(dtoScore.getTotalScore());
            entity.setStatus(1);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            scores.add(entity);
        }

        // 批量更新
        int result = scoreMapper.insertOrUpdateBatch(courseId, scores);
        log.info("成绩录入完成: courseId={}, 影响{}条记录", courseId, result);
    }

    /**
     * 导出成绩
     * @param courseId
     * @param courseName
     * @return
     */
    @Override
    public List<ScoreCourseExportVO> exportCourseScore(Long courseId, String courseName) {
        List<ScoreCourseExportVO> voList = scoreMapper.selectScoreCourseExportList(courseId,courseName);
        if (voList == null) {
            return new ArrayList<>();
        }
        return voList;
    }
}