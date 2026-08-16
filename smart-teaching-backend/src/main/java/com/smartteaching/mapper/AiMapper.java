package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.ai.AiSession;
import com.smartteaching.entity.ai.AiMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMapper extends BaseMapper<AiSession> {
}
