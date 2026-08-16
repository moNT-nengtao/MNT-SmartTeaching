package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.qa.Question;
import com.smartteaching.entity.qa.Reply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaMapper extends BaseMapper<Question> {
}
