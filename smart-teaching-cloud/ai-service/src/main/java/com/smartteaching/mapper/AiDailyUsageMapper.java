package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.ai.AiDailyUsage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * @ClassName AiDailyUsageMapper
 * @Description
 * @Author MNT
 * @Date 2026/9/2 23:10
 **/
@Mapper
public interface AiDailyUsageMapper extends BaseMapper<AiDailyUsage> {

    /**
     * 查询某用户某日的使用记录
     */
    @Select("SELECT * FROM ai_daily_usage WHERE user_id = #{userId} AND use_date = #{useDate}")
    AiDailyUsage selectByUserAndDate(@Param("userId") Long userId, @Param("useDate") LocalDate useDate);

    /**
     * 今日使用次数 +1（不存在则插入，利用唯一索引防并发重复）
     */
    @Insert("INSERT INTO ai_daily_usage (user_id, use_date, use_count) VALUES (#{userId}, #{useDate}, 1) " +
            "ON DUPLICATE KEY UPDATE use_count = use_count + 1")
    int incrementUsage(@Param("userId") Long userId, @Param("useDate") LocalDate useDate);
}
