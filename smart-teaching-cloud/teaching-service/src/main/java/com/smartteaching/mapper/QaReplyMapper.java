package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.qa.Reply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaReplyMapper extends BaseMapper<Reply> {
}
