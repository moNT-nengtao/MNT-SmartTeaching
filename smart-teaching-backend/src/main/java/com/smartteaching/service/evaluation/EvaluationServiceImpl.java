package com.smartteaching.service.evaluation;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartteaching.common.dto.evaluation.EvaluationSubmitDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.utils.RedisUtils;
import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import com.smartteaching.common.vo.evaluation.EvaluationCourseVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherRankingVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.evaluation.CourseEvaluation;
import com.smartteaching.entity.selection.SelectionRecord;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.EvaluationMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName EvaluationServiceImpl
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:03
 **/
@Service
@Slf4j
public class EvaluationServiceImpl implements  EvaluationService {
    @Resource
    private EvaluationMapper evaluationMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisUtils redisUtils;
    private static final long DASHBOARD_CACHE_MINUTE = 20;
    private static final String CACHE_PREFIX_EVALUATION = "dashboard:evaluation:";


    /**
     * 可评价课程列表
     * @param studentId
     * @return
     */
    @Override
    public List<EvaluationCourseVO> getEvaluableCourses(Long studentId) {
        if (studentId == null) {
            throw new BaseException("未查询到学生信息");
        }

        return evaluationMapper.selectEvaluableCourses(studentId);
    }

    /**
     * 提交课程评价
     * @param studentId
     * @param dto
     */
    @Override
    public void saveEvaluableCourse(Long studentId, EvaluationSubmitDTO dto) {
        //校验学生
        if (studentId == null) {
            throw new BaseException("未查询到学生信息");
        }
        //校验课程
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BaseException("课程不存在");
        }
        //校验是否可评价（防止前端出错）
        List<EvaluationCourseVO> list = evaluationMapper.selectEvaluableCourses(studentId);
        EvaluationCourseVO target = list.stream()
                .filter(item -> item.getId().equals(dto.getCourseId()))
                .findFirst()
                .orElseThrow(() -> new BaseException("该课程不在已选列表中"));
        //校验是否可评价
        if (target.getIsEvaluated()) {
            throw new BaseException("您已评价过该课程，请勿重复评价");
        }
        if (!target.getCanEvaluate()) {
            throw new BaseException("该课程暂无成绩，无法评价");
        }

        //计算综合评分
        double avgScore = (dto.getTeachingAbility() + dto.getClassAtmosphere()
                + dto.getKnowledgeClarity() + dto.getHomeworkFeedback() + dto.getQaService()) / 5.0;
        BigDecimal score = BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP);

        //保存评价
        CourseEvaluation evaluation = new CourseEvaluation();
        evaluation.setCourseId(dto.getCourseId());
        evaluation.setTeacherId(course.getTeacherId());
        evaluation.setStudentId(studentId);
        evaluation.setScore(score);
        evaluation.setTeachingAbility(BigDecimal.valueOf(dto.getTeachingAbility()));
        evaluation.setClassAtmosphere(BigDecimal.valueOf(dto.getClassAtmosphere()));
        evaluation.setKnowledgeClarity(BigDecimal.valueOf(dto.getKnowledgeClarity()));
        evaluation.setHomeworkFeedback(BigDecimal.valueOf(dto.getHomeworkFeedback()));
        evaluation.setQaService(BigDecimal.valueOf(dto.getQaService()));
        evaluation.setContent(dto.getComment());
        evaluation.setCreateTime(LocalDateTime.now());

        evaluationMapper.insert(evaluation);
    }

    /**
     * 课程评价统计
     * @param teacherId
     * @return
     */
    @Override
    public EvaluationTeacherVO evaluationDashboard(Long teacherId) {
        String cacheKey = RedisUtils.buildKey(CACHE_PREFIX_EVALUATION);

        //查缓存
        String cachedJson = redisUtils.getStr(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, EvaluationTeacherVO.class);
        }
        //缓存失效后，查数据库数据
        EvaluationTeacherVO vo = buildEvaluationTeacherDashboardData(teacherId);

        //写入缓存
        String json = JSON.toJSONString(vo);
        redisUtils.setStr(cacheKey, json, DASHBOARD_CACHE_MINUTE, TimeUnit.MINUTES);

        return vo;
    }

    /**
     * 按课程查询评价列表（教师）
     * @param courseId
     * @return
     */
    @Override
    public List<EvaluationTeacherVO.EvaluationItem> getEvaluationList(Long courseId) {
        // 查询该课程的所有评价
        LambdaQueryWrapper<CourseEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEvaluation::getCourseId, courseId)
                .orderByDesc(CourseEvaluation::getCreateTime);
        List<CourseEvaluation> evaluations = evaluationMapper.selectList(wrapper);

        // 转换为 VO
        return evaluations.stream().map(eval -> {
            EvaluationTeacherVO.EvaluationItem item = new EvaluationTeacherVO.EvaluationItem();
            item.setId(eval.getId());
            item.setCourseId(eval.getCourseId());
            item.setScore(eval.getScore());
            item.setComment(eval.getContent());
            item.setCreateTime(eval.getCreateTime());
            item.setTeachingAbility(eval.getTeachingAbility());
            item.setClassAtmosphere(eval.getClassAtmosphere());
            item.setKnowledgeClarity(eval.getKnowledgeClarity());
            item.setHomeworkFeedback(eval.getHomeworkFeedback());
            item.setQaService(eval.getQaService());

            // 学生姓名（脱敏）
            User student = userMapper.selectById(eval.getStudentId());
            if (student != null) {
                String realName = student.getRealName();
                if (realName != null && realName.length() > 1) {
                    item.setStudentName(realName.charAt(0) + "**");
                } else {
                    item.setStudentName(realName);
                }
            }
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 教师评价榜单
     * @param collegeId
     * @param subject
     * @return
     */
    @Override
    public List<EvaluationTeacherRankingVO> getEvaluationRanking(Long collegeId, String subject) {
        List<EvaluationTeacherRankingVO> list = evaluationMapper.selectTeacherRanking(collegeId, subject);
        return list;
    }

    /**
     * 课程评价统计数据
     * @param teacherId
     * @return
     */
    public EvaluationTeacherVO buildEvaluationTeacherDashboardData(Long teacherId) {
        // 1. 查询该教师的所有课程
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getTeacherId, teacherId)
                .eq(Course::getStatus, 1);
        List<Course> courses = courseMapper.selectList(courseWrapper);
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());

        // 默认空数据
        EvaluationTeacherVO vo = new EvaluationTeacherVO();
        if (courseIds.isEmpty()) {
            vo.setStatCards(new EvaluationTeacherVO.StatCards());
            vo.setRadarData(new EvaluationTeacherVO.RadarData());
            vo.setCourseList(new ArrayList<>());
            return vo;
        }

        // 2. 查询该教师所有课程的评价
        LambdaQueryWrapper<CourseEvaluation> evalWrapper = new LambdaQueryWrapper<>();
        evalWrapper.in(CourseEvaluation::getCourseId, courseIds)
                .orderByDesc(CourseEvaluation::getCreateTime);
        List<CourseEvaluation> evaluations = evaluationMapper.selectList(evalWrapper);

        // 3. 统计卡片数据
        EvaluationTeacherVO.StatCards statCards = new EvaluationTeacherVO.StatCards();
        if (!evaluations.isEmpty()) {
            // 综合评分
            BigDecimal avgScore = evaluations.stream()
                    .map(CourseEvaluation::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);
            statCards.setAvgScore(avgScore);

            // 评价人数（去重学生）
            long evalCount = evaluations.stream()
                    .map(CourseEvaluation::getStudentId)
                    .distinct()
                    .count();
            statCards.setEvaluationCount((int) evalCount);

            // 好评率（评分 >= 4 分）
            long goodCount = evaluations.stream()
                    .filter(e -> e.getScore() != null && e.getScore().compareTo(new BigDecimal("4.0")) >= 0)
                    .count();
            BigDecimal goodRate = BigDecimal.valueOf(goodCount)
                    .divide(BigDecimal.valueOf(evaluations.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
            statCards.setGoodRate(goodRate);

            // 被评课程数
            long courseCount = evaluations.stream()
                    .map(CourseEvaluation::getCourseId)
                    .distinct()
                    .count();
            statCards.setCourseCount((int) courseCount);
        } else {
            statCards.setAvgScore(BigDecimal.ZERO);
            statCards.setEvaluationCount(0);
            statCards.setGoodRate(BigDecimal.ZERO);
            statCards.setCourseCount(0);
        }
        vo.setStatCards(statCards);

        // 4. 雷达图数据（各维度平均分）
        EvaluationTeacherVO.RadarData radarData = new EvaluationTeacherVO.RadarData();
        if (!evaluations.isEmpty()) {
            int size = evaluations.size();
            radarData.setTeachingAbility(
                    evaluations.stream().map(CourseEvaluation::getTeachingAbility).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP)
            );
            radarData.setClassAtmosphere(
                    evaluations.stream().map(CourseEvaluation::getClassAtmosphere).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP)
            );
            radarData.setKnowledgeClarity(
                    evaluations.stream().map(CourseEvaluation::getKnowledgeClarity).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP)
            );
            radarData.setHomeworkFeedback(
                    evaluations.stream().map(CourseEvaluation::getHomeworkFeedback).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP)
            );
            radarData.setQaService(
                    evaluations.stream().map(CourseEvaluation::getQaService).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP)
            );
        } else {
            radarData.setTeachingAbility(BigDecimal.ZERO);
            radarData.setClassAtmosphere(BigDecimal.ZERO);
            radarData.setKnowledgeClarity(BigDecimal.ZERO);
            radarData.setHomeworkFeedback(BigDecimal.ZERO);
            radarData.setQaService(BigDecimal.ZERO);
        }
        vo.setRadarData(radarData);

        // 5. 教师授课课程列表
        List<EvaluationTeacherVO.CourseItem> courseItems = courseMapper.selectTeacherCoursesWithStat(teacherId);
        vo.setCourseList(courseItems);

        return vo;
    }

}
