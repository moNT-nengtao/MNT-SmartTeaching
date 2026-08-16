package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.attendance.AttendanceSession;
import com.smartteaching.entity.attendance.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceMapper extends BaseMapper<AttendanceSession> {
}
