# -*- coding: utf-8 -*-
"""修复跨服务共享实体与 Mapper：User 实体入 common；各服务 UserMapper 精简；course CourseMapper 去 evaluation 方法"""
import io, sys, os, shutil
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'

def w(path, content):
    p = ROOT + '\\' + path
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(content)
    print('WRITE', path)

# ===== A. User 实体迁入 common =====
src = open(ROOT + r'\uaa-service\src\main\java\com\smartteaching\entity\user\User.java', encoding='utf-8').read()
w(r'common\src\main\java\com\smartteaching\entity\user\User.java', src)
# 删除 uaa-service 的 User.java（避免与 common 重复类）
os.remove(ROOT + r'\uaa-service\src\main\java\com\smartteaching\entity\user\User.java')
print('REMOVE uaa User.java')

# ===== B. 各服务（uaa 除外）UserMapper 精简 =====
slim = '''package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（精简版，仅提供基础 CRUD）
 * 供 JWT 认证链路（loadUserByUsername / getCurrentUser）使用，
 * 用户域的列表/分页等能力统一由 uaa-service 提供（Feign 调用）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
'''
for svc in ['course-service','selection-score-service','attendance-service','warning-service','ai-service','analysis-service','teaching-service']:
    w(svc + r'\src\main\java\com\smartteaching\mapper\UserMapper.java', slim)

# ===== C. course-service CourseMapper 去掉 evaluation 域方法 =====
p = ROOT + r'\course-service\src\main\java\com\smartteaching\mapper\CourseMapper.java'
c = open(p, encoding='utf-8').read()
old_m = '''    /**
     * 查询教师课程列表（含选课人数、评价数、评分）
     * @param teacherId
     * @return
     */
    List<EvaluationTeacherVO.CourseItem> selectTeacherCoursesWithStat(@Param("teacherId") Long teacherId);
'''
assert c.count(old_m) == 1
c = c.replace(old_m, '')
old_i = 'import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;\n'
assert c.count(old_i) == 1
c = c.replace(old_i, '')
open(p, 'w', encoding='utf-8').write(c)
print('PATCH course-service CourseMapper')

# ===== D. 检查各服务（除 uaa）resources/mapper 下的 UserMapper.xml =====
for svc in ['course-service','selection-score-service','attendance-service','warning-service','ai-service','analysis-service','teaching-service']:
    xp = os.path.join(ROOT, svc, 'src', 'main', 'resources', 'mapper', 'UserMapper.xml')
    if os.path.exists(xp):
        os.remove(xp)
        print('REMOVE xml', svc, 'UserMapper.xml')

print('DONE')
