package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.dashboard.DashboardStat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper extends BaseMapper<DashboardStat> {
}
