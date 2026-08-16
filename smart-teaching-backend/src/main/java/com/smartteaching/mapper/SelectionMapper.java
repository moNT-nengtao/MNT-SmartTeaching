package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.selection.SelectionConfig;
import com.smartteaching.entity.selection.SelectionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SelectionMapper extends BaseMapper<SelectionConfig> {
}
