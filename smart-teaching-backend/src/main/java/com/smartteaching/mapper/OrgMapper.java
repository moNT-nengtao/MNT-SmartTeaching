package com.smartteaching.mapper;

import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 新增组织节点-学院
     * @param college
     */
    @Insert("insert into smart_teaching.org_college(name,code,parent_id,status,sort,create_time,update_time)\n" +
            "values(#{name},#{code},#{parentId},0,0,now(),now())")
    Integer saveCollege(College college);


    /**
     * 新增组织节点-专业
     * @param major
     */
    @Insert("insert into smart_teaching.org_major(name,code,college_id,status,sort,create_time,update_time)\n" +
            "values(#{name},#{code},#{collegeId},0,0,now(),now())")
    Integer saveMajor(Major major);

    /**
     * 新增组织节点-班级
     * @param classInfo
     */
    @Insert("insert into smart_teaching.org_class(name,code,major_id,grade_year,status,sort,create_time,update_time)\n" +
            "values(#{name},#{code},#{majorId},#{gradeYear},0,0,now(),now())")
    Integer saveClass(ClassInfo classInfo);

    /**
     * 编辑组织节点-学院
     * @param college
     * @return
     */
    @Update("update smart_teaching.org_college set name = #{name}, code = #{code}, parent_id = #{parentId} where id = #{id}")
    int updateCollege(College college);

    /**
     * 编辑组织节点-专业
     * @param major
     * @return
     */
    @Update("update smart_teaching.org_major set name = #{name} ,code = #{code} where id = #{id}")
    int updateMajor(Major major);

    /**
     * 编辑组织节点-班级
     * @param classInfo
     * @return
     */
    @Update("UPDATE smart_teaching.org_class SET name = #{name}, code = #{code}, grade_year = #{gradeYear} WHERE id = #{id}")
    int updateClass(ClassInfo classInfo);

    /**
     * 效验管理数据-学院
     * @param id
     * @return
     */
    @Select("SELECT COUNT(1) FROM org_major WHERE college_id = #{id} AND status = 1")
    Long countMajorsByCollegeId(Long id);

    /**
     * 效验管理数据-专业
     * @param id
     * @return
     */
    @Select("SELECT COUNT(1) FROM org_class WHERE major_id = #{id} AND status = 1")
    Long countClassesByMajorId(Long id);

    /**
     * 效验管理数据-班级
     * @param id
     * @return
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE class_id = #{id} AND status = 1")
    Long countStudentsByClassId(Long id);

    /**
     * 删除组织节点-学院
     * @param id
     */
    @Update("UPDATE org_college SET status = 0, update_time = NOW() WHERE id = #{id}")
    void deleteCollegeById(Long id);

    /**
     * 删除组织节点-专业
     * @param id
     */
    @Update("UPDATE org_major SET status = 0, update_time = NOW() WHERE id = #{id}")
    void deleteMajorById(Long id);

    /**
     * 删除组织节点-班级
     * @param id
     */
    @Update("UPDATE org_class SET status = 0, update_time = NOW() WHERE id = #{id}")
    void deleteClassById(Long id);
}
