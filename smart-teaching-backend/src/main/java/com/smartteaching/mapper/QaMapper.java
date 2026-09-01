package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.qa.QaQueryDTO;
import com.smartteaching.common.vo.qa.QaQuestionListVO;
import com.smartteaching.common.vo.qa.QaReplyVO;
import com.smartteaching.entity.qa.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaMapper extends BaseMapper<Question> {

    /**
     * 分页查询问题列表
     */
    IPage<QaQuestionListVO> selectQuestionPage(IPage<QaQuestionListVO> page,
                                               @Param("dto") QaQueryDTO dto);

    /**
     * 查询问题详情
     */
    QaQuestionListVO selectQuestionDetail(@Param("id") Long id);

    /**
     * 查询问题的回复列表
     */
    List<QaReplyVO> selectReplies(@Param("questionId") Long questionId);

    /**
     * 查询全部去重标签
     */
    List<String> selectAllTags();
}
