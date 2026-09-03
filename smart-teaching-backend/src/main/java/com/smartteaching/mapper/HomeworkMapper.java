package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.homework.HomeworkQueryDTO;
import com.smartteaching.common.vo.homework.HomeworkListVO;
import com.smartteaching.common.vo.homework.HomeworkStudentListVO;
import com.smartteaching.common.vo.homework.HomeworkSubmissionVO;
import com.smartteaching.entity.homework.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName HomeworkMapper
 * @Description 作业 Mapper
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    /**
     * 教师/管理员 分页查询作业列表
     */
    IPage<HomeworkListVO> selectHomeworkPage(IPage<HomeworkListVO> iPage, @Param("dto") HomeworkQueryDTO queryDTO);

    /**
     * 学生 分页查询作业列表（含提交状态和成绩）
     */
    IPage<HomeworkStudentListVO> selectStudentHomeworkPage(IPage<HomeworkStudentListVO> iPage,
                                                             @Param("dto") HomeworkQueryDTO queryDTO,
                                                             @Param("studentId") Long studentId);

    /**
     * 查询某作业的提交列表
     */
    List<HomeworkSubmissionVO> selectSubmissionList(@Param("homeworkId") Long homeworkId);

    /**
     * 查询某学生在某课程下的选课记录（判断学生是否选了该课）
     */
    Long countStudentCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
