package com.smartteaching.service.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.selection.SelectionQueryDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.WeekUtil;
import com.smartteaching.common.vo.selection.SelectionConfigQueryVO;
import com.smartteaching.common.vo.selection.SelectionMyCourseVO;
import com.smartteaching.common.vo.selection.SelectionQueryVO;
import com.smartteaching.common.vo.selection.SelectionStudentVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.score.Score;
import com.smartteaching.entity.selection.SelectionConfig;
import com.smartteaching.entity.selection.SelectionRecord;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.ScoreMapper;
import com.smartteaching.mapper.SelectionMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName SelectionServiceImpl
 * @Description 选课服务实现类
 * @Author MNT
 * @Date 2026/8/17 11:06
 **/
@Service
public class SelectionServiceImpl implements SelectionService{
    @Resource
    private ScoreMapper scoreMapper;
    @Resource
    private SelectionMapper selectionMapper;
    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取选课配置
     * @return
     */
    @Override
    public SelectionConfigQueryVO getSelectionConfig() {
        SelectionConfig entity = selectionMapper.selectOne(null);
        if (entity == null) {
            return new SelectionConfigQueryVO();
        }
        SelectionConfigQueryVO vo = new SelectionConfigQueryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 设置选课时间
     * @param config
     */
    @Override
    public void saveSelectionConfig(SelectionConfig config) {
        selectionMapper.updateById(config);
    }

    /**
     * 选课大厅
     *
     * @param selectionQueryDTO
     * @param userId
     * @return
     */
    @Override
    public PageResult<SelectionQueryVO> getSelectionQuery(SelectionQueryDTO selectionQueryDTO, Long userId) {
        long pageNum = selectionQueryDTO.getPageNum() == null ? 1 : selectionQueryDTO.getPageNum();
        long pageSize = selectionQueryDTO.getPageSize() == null ? 10 : selectionQueryDTO.getPageSize();
        IPage<SelectionQueryVO> iPage = new Page<>(pageNum,pageSize);
        IPage<SelectionQueryVO> voiPage = selectionMapper.selectSelectionByCourseName(iPage,selectionQueryDTO,userId);

        PageResult<SelectionQueryVO> pageResult = new PageResult<>();
        pageResult.setRecords(voiPage.getRecords());
        pageResult.setTotal(voiPage.getTotal());
        pageResult.setCurrent(voiPage.getCurrent());
        pageResult.setSize(voiPage.getSize());
        pageResult.setPages(voiPage.getPages());
        return pageResult;
    }

    /**
     * 课程选课学生名单
     * @param courseId
     * @return
     */
    @Override
    public PageResult<SelectionStudentVO> getSelectionStudernt(Long courseId, Integer pageNum, Integer pageSize) {
        IPage<SelectionStudentVO> iPage = new Page<>(pageNum,pageSize);
        IPage<SelectionStudentVO> voiPage = selectionMapper.selectCourseStudents(iPage, courseId);

        PageResult<SelectionStudentVO> pageResult = new PageResult<>();
        pageResult.setRecords(voiPage.getRecords());
        pageResult.setTotal(voiPage.getTotal());
        pageResult.setCurrent(voiPage.getCurrent());
        pageResult.setSize(voiPage.getSize());
        pageResult.setPages(voiPage.getPages());
        return pageResult;
    }


    /**
     * 智能推荐课程
     * TODO 未接入大模型，目前只根据用户id、同教师、选课率最高推荐的
     * @param userId
     * @return
     */
    @Override
    public List<SelectionQueryVO> getRecommendCourses(Long userId) {
        List<SelectionQueryVO> recommendList = new ArrayList<>();
        final int TARGET = 3;
        final int EXTRA = 2;

        //同教师
        int limitTeacher = TARGET + EXTRA;
        List<SelectionQueryVO> teacherList = selectionMapper.selectRecommendTeacher(userId, limitTeacher);
        teacherList.forEach(c -> c.setReason(MessageConstant.RECOMMEND_REASON_TEACHER));
        recommendList.addAll(teacherList);

        //同专业
        int gapMajor = TARGET - teacherList.size();
        int limitMajor = Math.max(gapMajor + EXTRA, 1);
        List<SelectionQueryVO> majorList = selectionMapper.selectRecommendMajor(userId, limitMajor);
        majorList.forEach(c -> c.setReason(MessageConstant.RECOMMEND_REASON_MAJOR));
        recommendList.addAll(majorList);

        //热门课程
        int gapHot = TARGET - recommendList.size();
        int limitHotCourse = Math.max(gapHot + EXTRA, 1);
        List<SelectionQueryVO> hotCourseList = selectionMapper.selectRecommendHotCourse(userId, limitHotCourse);
        hotCourseList.forEach(c -> c.setReason(MessageConstant.RECOMMEND_REASON_HOT_COURSE));
        recommendList.addAll(hotCourseList);

        //高分课程
        int gapHigh = TARGET - recommendList.size();
        int limitHighCourse = Math.max(gapHigh + EXTRA, 1);
        List<SelectionQueryVO> highCourseList = selectionMapper.selectRecommendHighCourse(userId, limitHighCourse);
        highCourseList.forEach(c -> c.setReason(MessageConstant.RECOMMEND_REASON_HIGH_SCORE_COURSE));
        recommendList.addAll(highCourseList);

        //去重
        List<SelectionQueryVO> distinctList = recommendList.stream()
                .collect(Collectors.collectingAndThen
                        (Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SelectionQueryVO::getCourseId))),
                                ArrayList::new));
        Collections.shuffle(distinctList);

        List<SelectionQueryVO> finalList = distinctList.stream().limit(TARGET).collect(Collectors.toList());

        return finalList;
    }

    /**
     * 学生选课
     * @param courseId 课程ID
     * @param studentId 学生ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void studentSaveCourses(Long courseId, Long studentId) {
        // 校验
        if (studentId == null) {
            throw new BaseException("该用户已不存在");
        }
        if (courseId == null) {
            throw new BaseException("该课程已不存在");
        }

        // 检查是否已经有效选课
        boolean hasEffective = selectionMapper.hasSelected(studentId, courseId);
        if (hasEffective) {
            throw new BaseException("请勿重复选课");
        }

        // 检查课程容量是否已满
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BaseException("课程不存在");
        }

        // 获取课程教师ID
        Long teacherId = courseMapper.selectById(courseId).getTeacherId();
        if (teacherId == null) {
            throw new BaseException("课程已不存在");
        }

        // 插入新选课记录
        SelectionRecord selectionRecord = new SelectionRecord();
        selectionRecord.setCourseId(courseId);
        selectionRecord.setStudentId(studentId);
        selectionMapper.studentSaveCourses(selectionRecord);

        // 插入成绩记录（默认成绩为空）
        Score score = new Score();
        score.setCourseId(courseId);
        score.setStudentId(studentId);
        score.setTeacherId(teacherId);
        score.setScore(null);
        score.setUsualScore(null);
        score.setFinalScore(null);
        score.setRemark("选课成功");
        score.setStatus(1);
        score.setCreateTime(LocalDateTime.now());
        score.setUpdateTime(LocalDateTime.now());
        scoreMapper.insert(score);

    }

    /**
     * 学生退课
     * @param courseId
     * @param studentId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void studentDeleteCourses(Long courseId, Long studentId) {
        if (studentId == null) {
            throw new BaseException(MessageConstant.OPERATE_USER_NOT_EXIST);
        }
        if (courseId == null) {
            throw new BaseException(MessageConstant.COURSE_NOT_EXIST);
        }

        // 物理删除选课记录
        int selectionResult = selectionMapper.deleteByCourseAndStudent(courseId, studentId);
        if (selectionResult == 0) {
            throw new BaseException("选课记录不存在");
        }

        // 检查成绩是否已录入(录入后不可以退了)
        LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(Score::getCourseId, courseId)
                .eq(Score::getStudentId, studentId)
                .eq(Score::getStatus, 1);
        Score score = scoreMapper.selectOne(scoreWrapper);
        if (score != null && score.getScore() != null) {
            throw new BaseException("该课程已录入成绩，无法退课");
        }

        // 物理删除成绩记录
        int scoreResult = scoreMapper.delete(scoreWrapper);
    }


    /**
     * 我的已选课程
     * @return
     */
    @Override
    public PageResult<SelectionMyCourseVO> getSelectionMyCourses(Long studentId, Integer pageNum, Integer pageSize) {
        IPage<SelectionMyCourseVO> iPage = new Page<>(pageNum,pageSize);
        IPage<SelectionMyCourseVO> voiPage = selectionMapper.getSelectionMyCourses(iPage, studentId);

        //week转换
        for (SelectionMyCourseVO vo : voiPage.getRecords()) {
            vo.setWeek(WeekUtil.jsonToRangeStr(vo.getWeek()));
        }

        PageResult<SelectionMyCourseVO> pageResult = new PageResult<>();
        pageResult.setRecords(voiPage.getRecords());
        pageResult.setTotal(voiPage.getTotal());
        pageResult.setCurrent(voiPage.getCurrent());
        pageResult.setSize(voiPage.getSize());
        pageResult.setPages(voiPage.getPages());
        return pageResult;
    }
}
