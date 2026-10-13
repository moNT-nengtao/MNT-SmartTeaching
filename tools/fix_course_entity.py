# -*- coding: utf-8 -*-
"""Course/CourseSchedule 实体迁入 common + 清理残留 Course import"""
import io, sys, os, re
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'

def w(path, content):
    p = ROOT + '\\' + path
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(content)
    print('WRITE', path)

# A. 实体迁入 common
for ent in ['Course.java', 'CourseSchedule.java']:
    src = os.path.join(ROOT, 'course-service', 'src', 'main', 'java', 'com', 'smartteaching', 'entity', 'course', ent)
    c = open(src, encoding='utf-8').read()
    w(os.path.join('common', 'src', 'main', 'java', 'com', 'smartteaching', 'entity', 'course', ent), c)
    os.remove(src)
    print('REMOVE course-service', ent)

# B. 检查并删除各文件残留的 Course 实体 import（确认无裸引用后删除）
files = [
    r'selection-score-service\src\main\java\com\smartteaching\service\score\ScoreServiceImpl.java',
    r'selection-score-service\src\main\java\com\smartteaching\service\selection\SelectionServiceImpl.java',
    r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
    r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
    r'teaching-service\src\main\java\com\smartteaching\service\qa\QaServiceImpl.java',
]
for rel in files:
    p = ROOT + '\\' + rel
    c = open(p, encoding='utf-8').read()
    imp_line = 'import com.smartteaching.entity.course.Course;\n'
    if imp_line in c:
        # 删除 import 前，检查文件是否还有 Course 裸引用（类型/构造器/静态引用）
        without = c.replace(imp_line, '')
        # 粗检：去掉 import 语句、注释、字符串后是否出现 \bCourse\b
        code = re.sub(r'/\*.*?\*/', '', without, flags=re.S)
        code = re.sub(r'//[^\n]*', '', code)
        code = re.sub(r'"(?:[^"\\]|\\.)*"', '', code)
        remaining = [m.start() for m in re.finditer(r'\bCourse\b', code)]
        # CourseInfoVO 也包含 Course 单词，需排除 CourseInfoVO
        remaining = [m for m in remaining if not code[m:m+len('CourseInfoVO')] == 'CourseInfoVO']
        if remaining:
            print('SKIP %s (仍有 Course 引用 %d 处)' % (rel, len(remaining)))
            continue
        c = without
        open(p, 'w', encoding='utf-8').write(c)
        print('PATCH', rel, '(删除残留 import)')
    else:
        print('SKIP %s (无 import)' % rel)

# C. 确认 teaching-service CourseMapper 的 CourseSchedule import 现在可解析（实体已在 common）
print('DONE')
