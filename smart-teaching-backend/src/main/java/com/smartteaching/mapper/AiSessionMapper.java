package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.ai.AiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiSessionMapper extends BaseMapper<AiSession> {

    /**
     *
     * @param userId
     * @return
     */
    @Select("SELECT * FROM ai_session WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<AiSession> selectByUserId(@Param("userId") Long userId);

}

