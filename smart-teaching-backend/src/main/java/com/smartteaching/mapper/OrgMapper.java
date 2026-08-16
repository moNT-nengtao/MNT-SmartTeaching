package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.entity.org.ClassInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrgMapper extends BaseMapper<College> {
}
