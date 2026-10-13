package com.smartteaching.service.selection;

import com.smartteaching.common.dto.selection.SelectionQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.selection.SelectionConfigQueryVO;
import com.smartteaching.common.vo.selection.SelectionMyCourseVO;
import com.smartteaching.common.vo.selection.SelectionQueryVO;
import com.smartteaching.common.vo.selection.SelectionStudentVO;
import com.smartteaching.entity.selection.SelectionConfig;

import java.util.List;

/**
 * @ClassName SelectionService
 * @Description 选课服务接口
 * @Author MNT
 * @Date 2026/8/17 10:28
 **/
public interface SelectionService {
    /**
     * 获取选课配置
     * @return
     */
    SelectionConfigQueryVO getSelectionConfig();

    /**
     * 设置选课时间
     * @param config
     */
    void saveSelectionConfig(SelectionConfig config);

    /**
     * 选课大厅
     *
     * @param selectionQueryDTO
     * @param userId
     * @return
     */
    PageResult<SelectionQueryVO> getSelectionQuery(SelectionQueryDTO selectionQueryDTO, Long userId);

    /**
     * 课程选课学生名单
     * @param courseId
     * @return
     */
    PageResult<SelectionStudentVO> getSelectionStudernt(Long courseId, Integer pageNum, Integer pageSize);

    /**
     * 智能推荐课程
     * @param userId
     * @return
     */
    List<SelectionQueryVO> getRecommendCourses(Long userId);

    /**
     * 学生选课
     * @param courseId
     * @param studentId
     */
    void studentSaveCourses(Long courseId, Long studentId);

    /**
     * 学生退课
     * @param courseId
     * @param studentId
     */
    void studentDeleteCourses(Long courseId, Long studentId);

    /**
     * 我的已选课程
     * @return
     */
    PageResult<SelectionMyCourseVO> getSelectionMyCourses(Long studentId, Integer pageNum, Integer pageSize);
}
