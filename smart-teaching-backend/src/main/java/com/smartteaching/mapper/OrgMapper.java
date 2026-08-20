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
            "values(#{name},#{code},#{parentId},1,#{sort},now(),now())")
    Integer saveCollege(College college);

    /**
     * 新增组织节点-专业
     * @param major
     */
    @Insert("insert into smart_teaching.org_major(name,code,college_id,status,sort,create_time,update_time)\n" +
            "values(#{name},#{code},#{collegeId},1,#{sort},now(),now())")
    Integer saveMajor(Major major);

    /**
     * 新增组织节点-班级
     * @param classInfo
     */
    @Insert("insert into smart_teaching.org_class(name,code,major_id,grade_year,status,sort,create_time,update_time)\n" +
            "values(#{name},#{code},#{majorId},#{gradeYear},1,#{sort},now(),now())")
    Integer saveClass(ClassInfo classInfo);

    /**
     * 编辑组织节点-学院
     * @param college
     * @return
     */
    @Update("update smart_teaching.org_college set name = #{name}, code = #{code}, parent_id = #{parentId}, sort = #{sort}, status = #{status}, update_time = now() where id = #{id}")
    int updateCollege(College college);

    /**
     * 编辑组织节点-专业
     * @param major
     * @return
     */
    @Update("update smart_teaching.org_major set name = #{name}, code = #{code}, college_id = #{collegeId}, sort = #{sort}, status = #{status}, update_time = now() where id = #{id}")
    int updateMajor(Major major);

    /**
     * 编辑组织节点-班级
     * @param classInfo
     * @return
     */
    @Update("update smart_teaching.org_class set name = #{name}, code = #{code}, major_id = #{majorId}, grade_year = #{gradeYear}, sort = #{sort}, status = #{status}, update_time = now() where id = #{id}")
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

    /**
     * 判断college表id是否存在
     * @param id
     * @return
     */
    @Select("SELECT COUNT(1) FROM org_college WHERE id = #{id}")
    Long countCollegeById(@Param("id") Long id);

    /**
     * 判断major表id是否存在
     * @param id
     * @return
     */
    @Select("SELECT COUNT(1) FROM org_major WHERE id = #{id}")
    Long countMajorById(@Param("id") Long id);

    /**
     * 根据ID查询学院（含已删除）
     */
    @Select("SELECT * FROM org_college WHERE id = #{id}")
    College getCollegeById(@Param("id") Long id);

    /**
     * 根据ID查询专业（含已删除）
     */
    @Select("SELECT * FROM org_major WHERE id = #{id}")
    Major getMajorById(@Param("id") Long id);

    /**
     * 根据ID查询班级（含已删除）
     */
    @Select("SELECT * FROM org_class WHERE id = #{id}")
    ClassInfo getClassById(@Param("id") Long id);

    /**
     * 根据名称查询学院（含已删除）
     */
    @Select("SELECT * FROM org_college WHERE name = #{name}")
    College getCollegeByNameAllStatus(@Param("name") String name);

    /**
     * 根据名称查询专业（含已删除）
     */
    @Select("SELECT * FROM org_major WHERE name = #{name}")
    Major getMajorByNameAllStatus(@Param("name") String name);

    /**
     * 根据名称查询班级（含已删除）
     */
    @Select("SELECT * FROM org_class WHERE name = #{name}")
    ClassInfo getClassByNameAllStatus(@Param("name") String name);

    //========UserExcelValidateUtil 调用=========
    /**
     * 根据学院名称查询 ID
     */
    Long getCollegeIdByName(@Param("name") String name);

    /**
     * 根据专业名称查询 ID
     */
    Long getMajorIdByName(@Param("name") String name);

    /**
     * 根据班级名称查询 ID
     */
    Long getClassIdByName(@Param("name") String name);

    /**
     * 批量查询（根据名称列表）
     */
    List<College> getCollegesByNames(@Param("names") List<String> names);
    List<Major> getMajorsByNames(@Param("names") List<String> names);
    List<ClassInfo> getClassesByNames(@Param("names") List<String> names);

    /**
     * 批量插入
     * @param newColleges
     * @return
     */
    int batchInsertColleges(List<College> newColleges);
    int batchInsertMajors(List<Major> newMajors);
    int batchInsertClasses(List<ClassInfo> newClasses);
}