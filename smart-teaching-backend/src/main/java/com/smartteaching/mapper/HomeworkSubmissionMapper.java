package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.homework.HomeworkSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName HomeworkSubmissionMapper
 * @Description 作业提交 Mapper
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmission> {
}
