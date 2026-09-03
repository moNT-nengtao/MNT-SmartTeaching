package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.ai.AiMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName AiMessageMapper
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:12
 **/
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {

    /**
     *
     * @param sessionId
     * @return
     */
    @Select("SELECT * FROM ai_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<AiMessage> selectBySessionIdOrderByCreateTime(@Param("sessionId") Long sessionId);
}