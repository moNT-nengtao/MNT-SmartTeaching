# -*- coding: utf-8 -*-
"""teaching-service 修复：CourseMapper 精简 + 删除不可解析 import"""
import io, sys, os
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'

# 1. CourseMapper.java 精简
p = os.path.join(ROOT, 'teaching-service', 'src', 'main', 'java', 'com', 'smartteaching', 'mapper', 'CourseMapper.java')
c = open(p, encoding='utf-8').read()
slim = '''package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;
import com.smartteaching.entity.course.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程 Mapper（精简版，仅保留评价域聚合统计查询）
 * 课程域完整 Mapper 在 course-service；本服务仅通过共享库执行评价统计 SQL
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 查询教师课程列表（含选课人数、评价数、评分）
     * @param teacherId
     * @return
     */
    List<EvaluationTeacherVO.CourseItem> selectTeacherCoursesWithStat(@Param("teacherId") Long teacherId);
}
'''
open(p, 'w', encoding='utf-8').write(slim)
print('PATCH CourseMapper.java')

# 2. CourseMapper.xml 精简
p = os.path.join(ROOT, 'teaching-service', 'src', 'main', 'resources', 'mapper', 'CourseMapper.xml')
c = open(p, encoding='utf-8').read()
start = c.find('    <!--查询教师课程列表')
assert start > 0
head = c[:c.find('<mapper')]  # 保留 XML 头
tail_start = c.find('<select id="selectTeacherCoursesWithStat"')
tail_end = c.find('</mapper>')
xml = (head
       + '<mapper namespace="com.smartteaching.mapper.CourseMapper">\n'
       + c[tail_start:tail_end]
       + '</mapper>\n')
open(p, 'w', encoding='utf-8').write(xml)
print('PATCH CourseMapper.xml')

# 3. EvaluationServiceImpl 删除不可解析 import
p = os.path.join(ROOT, 'teaching-service', 'src', 'main', 'java', 'com', 'smartteaching', 'service', 'evaluation', 'EvaluationServiceImpl.java')
c = open(p, encoding='utf-8').read()
for imp in ['import com.smartteaching.common.vo.dashborad.DashboardAdminVO;\n',
            'import com.smartteaching.entity.selection.SelectionRecord;\n']:
    assert c.count(imp) == 1, imp
    c = c.replace(imp, '')
open(p, 'w', encoding='utf-8').write(c)
print('PATCH EvaluationServiceImpl imports')

# 4. 顺带检查 teaching-service 里 User 实体 import（common 已有，可解析）无需处理
print('DONE')
