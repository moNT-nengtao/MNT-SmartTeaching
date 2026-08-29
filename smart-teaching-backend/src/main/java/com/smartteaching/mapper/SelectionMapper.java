package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.selection.SelectionQueryDTO;
import com.smartteaching.common.vo.selection.SelectionMyCourseVO;
import com.smartteaching.common.vo.selection.SelectionQueryVO;
import com.smartteaching.common.vo.selection.SelectionStudentVO;
import com.smartteaching.entity.selection.SelectionConfig;
import com.smartteaching.entity.selection.SelectionRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectionMapper extends BaseMapper<SelectionConfig> {
    /**
     * 选课大厅
     *
     * @param iPage
     * @param selectionQueryDTO
     * @param userId
     * @return
     */
    IPage<SelectionQueryVO> selectSelectionByCourseName(IPage<SelectionQueryVO> iPage, @Param("dto") SelectionQueryDTO selectionQueryDTO, Long userId);

    /**
     * 课程选课学生名单
     * @param iPage
     * @param courseId
     * @return
     */
    IPage<SelectionStudentVO> selectCourseStudents(IPage<SelectionStudentVO> iPage, Long courseId);

    /**
     * 智能推荐课程
     * @param userId 用户id
     * @param limitTeacher 条数
     * @return 课程列表
     */

    List<SelectionQueryVO> selectRecommendTeacher(@Param("userId") Long userId, @Param("limit") int limitTeacher);

    List<SelectionQueryVO> selectRecommendMajor(@Param("userId") Long userId, @Param("limit") int limitMajor);

    List<SelectionQueryVO> selectRecommendHotCourse(@Param("userId") Long userId, @Param("limit") int limitHotCourse);

    List<SelectionQueryVO> selectRecommendHighCourse(@Param("userId") Long userId, @Param("limit") int limitHighCourse);

    /**
     * 学生选课
     */
    void studentSaveCourses(SelectionRecord selectionRecord);
    //判断是否已经有效选课
    boolean hasSelected(Long studentId, Long courseId);
    //恢复旧退课记录
    int recoverSelectCourse(Long studentId, Long courseId);

    /**
     * 学生退课
     * @param courseId
     * @param studentId
     */
    void studentDeleteCourses(Long courseId, Long studentId);

    /**
     * 我的已选课程
     * @param iPage
     * @param studentId
     * @return
     */
    IPage<SelectionMyCourseVO> getSelectionMyCourses(IPage<SelectionMyCourseVO> iPage, Long studentId);

    /**
     * 物理删除选课记录
     * @param courseId
     * @param studentId
     * @return
     */
    @Delete("DELETE FROM selection_record WHERE course_id = #{courseId} AND student_id = #{studentId}")
    int deleteByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
