package com.smartteaching.service.dashboard;

import com.alibaba.fastjson2.JSON;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.utils.RedisUtils;
import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import com.smartteaching.common.vo.dashborad.DashboardStudentVO;
import com.smartteaching.common.vo.dashborad.DashboardTeacherVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.DashboardMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @ClassName DashboardServiceImpl
 * @Description
 * @Author MNT
 * @Date 2026/8/27 09:50
 **/
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtils redisUtils;
    private static final long DASHBOARD_CACHE_MINUTE = 20;
    // 缓存Key前缀
    private static final String CACHE_PREFIX_ADMIN = "dashboard:admin";
    private static final String CACHE_PREFIX_TEACHER = "dashboard:teacher";
    private static final String CACHE_PREFIX_STUDENT = "dashboard:student";

    /**
     * 管理员仪表盘缓存
     * @return
     */
    @Override
    public DashboardAdminVO getAdminDashboard() {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_ADMIN);

        // 查缓存
        String cachedJson = redisUtils.getStr(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, DashboardAdminVO.class);
        }

        // 缓存未命中，查询数据库组装数据
        DashboardAdminVO vo = buildAdminDashboardData();

        // 3. 写入缓存（JSON字符串形式）
        String json = JSON.toJSONString(vo);
        redisUtils.setStr(cacheKey, json, DASHBOARD_CACHE_MINUTE, TimeUnit.MINUTES);

        return vo;
    }


    /**
     * 管理员仪表盘数据
     * @return
     */
    public DashboardAdminVO buildAdminDashboardData() {
        DashboardAdminVO vo = new DashboardAdminVO();

        //1.统计卡片
        DashboardAdminVO.StatCards statCards = dashboardMapper.selectDashboardAdminCard();
        vo.setStatCards(statCards);

        //2.各学院学生分布柱状图
        List<Map<String, Object>> collegeStudentList = dashboardMapper.selectCollegeStudentCount();
        DashboardAdminVO.ChartData<String,Long> barChart = new DashboardAdminVO.ChartData<>();
        List<String> barCategories = new ArrayList<>();
        List<Long> barValues = new ArrayList<>();
        for(Map<String,Object> item : collegeStudentList){
            barCategories.add((String)item.get("collegeName"));
            barValues.add(((Number)item.get("studentNum")).longValue());
        }
        DashboardAdminVO.Series<Long> barSeries = new DashboardAdminVO.Series<>();
        barSeries.setName("学生数量");
        barSeries.setData(barValues);
        barChart.setCategories(barCategories);
        barChart.setSeries(Collections.singletonList(barSeries));
        vo.setCollegeStudentDistribution(barChart);

        //3.师生比例饼图
        Map<String,Long> teacherStudentMap = dashboardMapper.selectTeacherStudentTotal();
        Long tCount = teacherStudentMap.get("teacherCount");
        Long sCount = teacherStudentMap.get("studentCount");
        DashboardAdminVO.ChartData<String,Double> pieChart = new DashboardAdminVO.ChartData<>();
        pieChart.setCategories(Arrays.asList("教师","学生"));
        DashboardAdminVO.Series<Double> pieSeries = new DashboardAdminVO.Series<>();
        pieSeries.setName("人数");
        pieSeries.setData(Arrays.asList(tCount.doubleValue(), sCount.doubleValue()));
        pieChart.setSeries(Collections.singletonList(pieSeries));
        vo.setTeacherStudentRatio(pieChart);

        //4.近7日系统活跃度折线图
        List<String> last7Day = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy‑MM‑dd");
        for(int i=6;i>=0;i--){
            LocalDate day = LocalDate.now().minusDays(i);
            last7Day.add(day.format(fmt));
        }
        List<Map<String,Object>> activeRaw = dashboardMapper.selectWeeklyActiveUser(last7Day);
        //转map方便填充缺省日期
        Map<String,Long> activeMap = new HashMap<>();
        for(Map<String,Object> m : activeRaw){
            String d = (String)m.get("day");
            Long num = ((Number)m.get("activeNum")).longValue();
            activeMap.put(d,num);
        }
        DashboardAdminVO.ChartData<String,Long> lineChart = new DashboardAdminVO.ChartData<>();
        lineChart.setCategories(last7Day);
        List<Long> lineData = new ArrayList<>();
        for(String d : last7Day){
            lineData.add(activeMap.getOrDefault(d,0L));
        }
        DashboardAdminVO.Series<Long> lineSeries = new DashboardAdminVO.Series<>();
        lineSeries.setName("活跃用户数");
        lineSeries.setData(lineData);
        lineChart.setSeries(Collections.singletonList(lineSeries));
        vo.setWeeklyActivity(lineChart);

        return vo;
    }


    /**
     * 教师仪表盘缓存
     * @return
     */
    @Override
    public DashboardTeacherVO getTeacherDashboard(Long teacherId) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_TEACHER);

        String cachedJson = redisUtils.getStr(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, DashboardTeacherVO.class);
        }

        DashboardTeacherVO vo = buildAdminDashboardData(teacherId);

        String json = JSON.toJSONString(vo);
        redisUtils.setStr(cacheKey, json, DASHBOARD_CACHE_MINUTE, TimeUnit.MINUTES);

        return vo;
    }

    /**
     * 教师仪表盘数据
     * @return
     */
    public DashboardTeacherVO buildAdminDashboardData(Long teacherId) {
        if (teacherId == null){
            throw new BaseException("未查询到教师信息");
        }
        User user = userMapper.selectById(teacherId);
        if (user == null || !"teacher".equals(user.getRole())) {
            throw new BaseException("非教师用户");
        }

        DashboardTeacherVO vo = new DashboardTeacherVO();

        //统计卡片
        DashboardTeacherVO.TeacherStatCards statCards = dashboardMapper.selectDashboardTeacherCard(teacherId);
        if (statCards == null) {
            statCards = new DashboardTeacherVO.TeacherStatCards();
            statCards.setCourseCount(0);
            statCards.setStudentTotal(0);
            statCards.setAvgScore(BigDecimal.ZERO);
        }
        //教师平均评价分数
        BigDecimal avgEvaluate = dashboardMapper.selectTeacherAvgEvaluate(teacherId);
        statCards.setAvgEvaluate(avgEvaluate == null ? BigDecimal.ZERO : avgEvaluate);
        vo.setStatCards(statCards);

        //成绩分布柱状图
        Map<String, Object> segmentMap = dashboardMapper.selectTeacherScoreSegment(teacherId);
        DashboardTeacherVO.ChartSeriesVO chartSeries = new DashboardTeacherVO.ChartSeriesVO();
        chartSeries.setCategories(List.of("0‑59","60‑69","70‑79","80‑89","90‑100"));

        List<Integer> dataList = new ArrayList<>();
        dataList.add(((Number) segmentMap.getOrDefault("s0", 0)).intValue());
        dataList.add(((Number) segmentMap.getOrDefault("s60", 0)).intValue());
        dataList.add(((Number) segmentMap.getOrDefault("s70", 0)).intValue());
        dataList.add(((Number) segmentMap.getOrDefault("s80", 0)).intValue());
        dataList.add(((Number) segmentMap.getOrDefault("s90", 0)).intValue());

        DashboardTeacherVO.ChartSeriesVO.SeriesItem seriesItem = new DashboardTeacherVO.ChartSeriesVO.SeriesItem();
        seriesItem.setName("学生人数");
        seriesItem.setData(dataList);
        chartSeries.setSeries(Collections.singletonList(seriesItem));
        vo.setScoreDistribution(chartSeries);

        //近7日考勤趋势
        //日期列表
        List<String> last7Days = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for(int i=6;i>=0;i--){
            last7Days.add(LocalDate.now().minusDays(i).format(fmt));
        }

        List<Map<String,Object>> attendanceTrend = dashboardMapper.selectTeacherAttendanceTrend(teacherId);
        Map<String,Double> attendanceMap = new HashMap<>();
        for(Map<String,Object> m : attendanceTrend){
            String d = (String)m.get("day");
            Double rate = ((Number) m.get("rate")).doubleValue();
            attendanceMap.put(d,rate);
        }
        List<Double> trendData = new ArrayList<>();
        for(String d : last7Days){
            trendData.add(attendanceMap.getOrDefault(d,0.0d));
        }
        DashboardTeacherVO.ChartSeriesVO trendChart = new DashboardTeacherVO.ChartSeriesVO();
        trendChart.setCategories(last7Days);
        DashboardTeacherVO.ChartSeriesVO.SeriesItem trendSeries = new DashboardTeacherVO.ChartSeriesVO.SeriesItem();
        trendSeries.setName("签到率(%)");
        trendSeries.setData(trendData);
        trendChart.setSeries(Collections.singletonList(trendSeries));
        vo.setAttendanceTrend(trendChart);

        return vo;
    }

    /**
     * 学生仪表盘缓存
     * @return
     */
    @Override
    public DashboardStudentVO getStudentDashboard(Long studentId) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_STUDENT);

        String cachedJson = redisUtils.getStr(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, DashboardStudentVO.class);
        }

        DashboardStudentVO vo = buildStudentDashboardData(studentId);

        String json = JSON.toJSONString(vo);
        redisUtils.setStr(cacheKey, json, DASHBOARD_CACHE_MINUTE, TimeUnit.MINUTES);

        return vo;
    }

    /**
     * 学生仪表盘数据
     * @return
     */
    public DashboardStudentVO buildStudentDashboardData(Long studentId) {
        DashboardStudentVO vo = new DashboardStudentVO();

        // ==================== 1. 统计卡片 ====================
        DashboardStudentVO.StudentStatCards cards = new DashboardStudentVO.StudentStatCards();
        //成绩表计算加权平均分
        BigDecimal gpa = dashboardMapper.calculateGPA(studentId);
        cards.setGpa(gpa != null ? gpa.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        //已修学分
        BigDecimal credit = dashboardMapper.calculateFinishedCredit(studentId);
        cards.setFinishedCredit(credit != null ? credit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        //本月考勤率
        BigDecimal rate = dashboardMapper.calculateMonthlyAttendanceRate(studentId);
        cards.setAttendanceRate(rate != null ? rate.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        //挂科科目数
        Integer failCount = dashboardMapper.countFailSubjects(studentId);
        cards.setFailSubjectCount(failCount != null ? failCount : 0);

        vo.setStatCards(cards);

        // ==================== 2. 各科成绩雷达图 ====================
        DashboardStudentVO.RadarChartVO radar = new DashboardStudentVO.RadarChartVO();
        List<Map<String, Object>> scores = dashboardMapper.getStudentScores(studentId);

        if (scores != null && !scores.isEmpty()) {
            // 取前8门课，避免雷达图太拥挤
            List<Map<String, Object>> topScores = scores.stream().limit(8).collect(Collectors.toList());

            List<DashboardStudentVO.RadarChartVO.RadarIndicator> indicators = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();

            for (Map<String, Object> item : topScores) {
                String courseName = (String) item.get("course_name");
                if (courseName != null && courseName.length() > 8) {
                    courseName = courseName.substring(0, 8) + "..";
                }
                BigDecimal score = (BigDecimal) item.get("score");

                DashboardStudentVO.RadarChartVO.RadarIndicator indicator = new DashboardStudentVO.RadarChartVO.RadarIndicator();
                indicator.setName(courseName != null ? courseName : "未知课程");
                indicator.setMax(100);
                indicators.add(indicator);
                // 收集该科目的分数
                values.add(score != null ? score.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            }

            radar.setIndicator(indicators);
            // 构建雷达图的数据系列
            List<DashboardStudentVO.RadarChartVO.RadarData> dataList = new ArrayList<>();
            DashboardStudentVO.RadarChartVO.RadarData data = new DashboardStudentVO.RadarChartVO.RadarData();
            data.setName("我的分数");
            data.setValue(values);
            dataList.add(data);
            radar.setData(dataList);
        } else {
            radar.setIndicator(new ArrayList<>());
            radar.setData(new ArrayList<>());
        }
        vo.setSubjectRadar(radar);

        // ==================== 3. 绩点趋势 ====================
        DashboardStudentVO.LineChartVO trend = new DashboardStudentVO.LineChartVO();
        // 按学期分组查询平均成绩
        List<Map<String, Object>> semesterScores = dashboardMapper.getScoresBySemester(studentId);

        if (semesterScores != null && !semesterScores.isEmpty()) {
            List<String> categories = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();

            for (Map<String, Object> item : semesterScores) {
                String semester = (String) item.get("semester");
                BigDecimal avgScore = (BigDecimal) item.get("avg_score");

                // 将百分制平均分转换为4.0制GPA
                BigDecimal gpaValue = convertScoreToGPA(avgScore);

                categories.add(semester != null ? semester : "未知学期");
                values.add(gpaValue != null ? gpaValue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            }

            trend.setCategories(categories);
            // 构建折线图的数据系列
            List<DashboardStudentVO.LineChartVO.SeriesItem> seriesList = new ArrayList<>();
            DashboardStudentVO.LineChartVO.SeriesItem series = new DashboardStudentVO.LineChartVO.SeriesItem();
            series.setName("GPA");
            series.setData(values);
            seriesList.add(series);
            trend.setSeries(seriesList);
        } else {
            trend.setCategories(new ArrayList<>());
            trend.setSeries(new ArrayList<>());
        }
        vo.setGpaTrend(trend);

        // ==================== 4. 月度考勤统计 ====================
        DashboardStudentVO.ChartSeriesVO attendanceChart = new DashboardStudentVO.ChartSeriesVO();
        // 查询近6个月的月度考勤率数据
        List<Map<String, Object>> monthlyData = dashboardMapper.getMonthlyAttendance(studentId);

        if (monthlyData != null && !monthlyData.isEmpty()) {
            List<String> categories = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();

            for (Map<String, Object> item : monthlyData) {
                String month = (String) item.get("month");
                BigDecimal attendanceRate = (BigDecimal) item.get("rate");
                //转换格式
                String displayMonth = month != null ? month.replace("-", "/") : "未知";
                categories.add(displayMonth);
                values.add(attendanceRate != null ? attendanceRate.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            }

            attendanceChart.setCategories(categories);
            // 构建考勤率数据系列
            List<DashboardStudentVO.ChartSeriesVO.SeriesItem> seriesList = new ArrayList<>();
            DashboardStudentVO.ChartSeriesVO.SeriesItem series = new DashboardStudentVO.ChartSeriesVO.SeriesItem();
            series.setName("考勤率");
            series.setData(values);
            seriesList.add(series);
            attendanceChart.setSeries(seriesList);
        } else {
            attendanceChart.setCategories(new ArrayList<>());
            attendanceChart.setSeries(new ArrayList<>());
        }
        vo.setAttendanceMonth(attendanceChart);

        // ==================== 5. 各科成绩对比（柱状图） ====================
        DashboardStudentVO.ChartSeriesVO scoreCompare = new DashboardStudentVO.ChartSeriesVO();
        // 查询各科成绩详情
        List<Map<String, Object>> scoreDetails = dashboardMapper.getScoreDetail(studentId);

        if (scoreDetails != null && !scoreDetails.isEmpty()) {
            List<String> categories = new ArrayList<>();
            List<BigDecimal> usualScores = new ArrayList<>();
            List<BigDecimal> finalScores = new ArrayList<>();
            List<BigDecimal> totalScores = new ArrayList<>();

            for (Map<String, Object> item : scoreDetails) {
                String courseName = (String) item.get("course_name");
                if (courseName != null && courseName.length() > 10) {
                    courseName = courseName.substring(0, 10) + "..";
                }
                categories.add(courseName != null ? courseName : "未知课程");

                BigDecimal usual = (BigDecimal) item.get("usual_score");
                BigDecimal finalScore = (BigDecimal) item.get("final_score");
                BigDecimal total = (BigDecimal) item.get("score");

                usualScores.add(usual != null ? usual.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                finalScores.add(finalScore != null ? finalScore.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                totalScores.add(total != null ? total.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            }

            scoreCompare.setCategories(categories);
            // 构建3组柱状图数据系列
            List<DashboardStudentVO.ChartSeriesVO.SeriesItem> seriesList = new ArrayList<>();

            DashboardStudentVO.ChartSeriesVO.SeriesItem usualSeries = new DashboardStudentVO.ChartSeriesVO.SeriesItem();
            usualSeries.setName("平时成绩");
            usualSeries.setData(usualScores);
            seriesList.add(usualSeries);

            DashboardStudentVO.ChartSeriesVO.SeriesItem finalSeries = new DashboardStudentVO.ChartSeriesVO.SeriesItem();
            finalSeries.setName("期末成绩");
            finalSeries.setData(finalScores);
            seriesList.add(finalSeries);

            DashboardStudentVO.ChartSeriesVO.SeriesItem totalSeries = new DashboardStudentVO.ChartSeriesVO.SeriesItem();
            totalSeries.setName("总评成绩");
            totalSeries.setData(totalScores);
            seriesList.add(totalSeries);

            scoreCompare.setSeries(seriesList);
        } else {
            scoreCompare.setCategories(new ArrayList<>());
            scoreCompare.setSeries(new ArrayList<>());
        }
        vo.setScoreCompare(scoreCompare);

        return vo;
    }

    //暂时无用
    /**
     * 清除管理员仪表盘缓存
     */
    public void evictAdminCache() {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_ADMIN);
        redisUtils.delete(cacheKey);
    }

    /**
     * 清除指定教师的仪表盘缓存
     */
    public void evictTeacherCache(Long teacherId) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_TEACHER, teacherId);
        redisUtils.delete(cacheKey);
    }

    /**
     * 清除指定学生的仪表盘缓存
     */
    public void evictStudentCache(Long studentId) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_STUDENT, studentId);
        redisUtils.delete(cacheKey);
    }



    /**
     * 将百分制成绩转换为绩点（4.0制）
     * 90-100: 4.0, 85-89: 3.7, 82-84: 3.3, 78-81: 3.0, 75-77: 2.7,
     * 72-74: 2.3, 68-71: 2.0, 64-67: 1.5, 60-63: 1.0, <60: 0
     */
    private BigDecimal convertScoreToGPA(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO;
        }

        int intScore = score.intValue();

        if (intScore >= 90) {
            return new BigDecimal("4.0");
        } else if (intScore >= 85) {
            return new BigDecimal("3.7");
        } else if (intScore >= 82) {
            return new BigDecimal("3.3");
        } else if (intScore >= 78) {
            return new BigDecimal("3.0");
        } else if (intScore >= 75) {
            return new BigDecimal("2.7");
        } else if (intScore >= 72) {
            return new BigDecimal("2.3");
        } else if (intScore >= 68) {
            return new BigDecimal("2.0");
        } else if (intScore >= 64) {
            return new BigDecimal("1.5");
        } else if (intScore >= 60) {
            return new BigDecimal("1.0");
        } else {
            return BigDecimal.ZERO;
        }
    }




}
