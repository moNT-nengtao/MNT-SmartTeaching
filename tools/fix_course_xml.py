# -*- coding: utf-8 -*-
import io, sys, re, os, glob
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
base = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
p = base + r'\course-service\src\main\resources\mapper\CourseMapper.xml'
c = open(p, encoding='utf-8', errors='ignore').read()
pattern = re.compile(r'<!--\u67e5\u8be2\u6559\u5e08\u8bfe\u7a0b\u5217\u8868.*?</select>', re.S)
c2, n = pattern.subn('', c)
print('course CourseMapper.xml 删除块数:', n)
open(p, 'w', encoding='utf-8', newline='\r\n').write(c2)

# 全量扫描：mapper xml 引用的 com.smartteaching 类型必须存在于某个模块源码
missing = []
for x in glob.glob(base + r'\**\src\main\resources\mapper\*.xml', recursive=True):
    c = open(x, encoding='utf-8', errors='ignore').read()
    for m in re.finditer(r'(?:resultType|parameterType)="(com\.smartteaching[^"]+)"', c):
        cls = m.group(1)
        inner = cls.split('$')[0]
        javapath = inner.replace('.', '\\') + '.java'
        found = False
        for j in glob.glob(base + r'\**\src\main\java\**' + javapath, recursive=True):
            found = True
        if not found:
            missing.append((cls, os.path.relpath(x, base)))
if missing:
    for cls, rel in missing:
        print('缺失类:', cls, '->', rel)
else:
    print('扫描完成：无缺失类引用')
