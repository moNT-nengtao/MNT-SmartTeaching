package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.warning.Warning;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarningMapper extends BaseMapper<Warning> {
}
