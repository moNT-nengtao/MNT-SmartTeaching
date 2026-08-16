package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.score.Score;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
}
