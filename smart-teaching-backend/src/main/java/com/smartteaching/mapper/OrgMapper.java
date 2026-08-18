package com.smartteaching.mapper;

import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrgMapper {

    /**
     * 查询学院列表
     * @param status 状态，null代表查询全部
     */
    List<College> selectCollegeList(@Param("status") Integer status);

    /**
     * 查询专业列表
     * @param collegeId 学院id
     * @param status 状态，null代表查询全部
     */
    List<Major> selectMajorList(@Param("collegeId") Long collegeId, @Param("status") Integer status);

    /**
     * 查询班级列表
     * @param majorId 专业id
     * @param status 状态，null代表查询全部
     */
    List<ClassInfo> selectClassList(@Param("majorId") Long majorId, @Param("status") Integer status);
}
