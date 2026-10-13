# -*- coding: utf-8 -*-
"""定位文件上传/访问 Controller 与 uploads 目录归属"""
import io, sys, os, re, glob
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
root = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
print('==== 各服务中涉及上传的 Controller ====')
for svc in ['uaa-service','course-service','selection-score-service','attendance-service','warning-service','ai-service','analysis-service','teaching-service']:
    base = os.path.join(root, svc, 'src', 'main', 'java')
    for dp, dn, fn in os.walk(base):
        for f in fn:
            if f.endswith('.java'):
                p = os.path.join(dp, f)
                c = open(p, encoding='utf-8').read()
                if 'MultipartFile' in c and 'Controller' in f:
                    print('  %s/%s' % (svc, f))
print('==== 单体 uploads 目录 ====')
mono = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-backend'
if os.path.isdir(mono + r'\uploads'):
    for p in os.listdir(mono + r'\uploads'):
        print('  ', p)
print('==== 单体中文件上传 Controller ====')
for dp, dn, fn in os.walk(mono + r'\src\main\java'):
    for f in fn:
        if f.endswith('.java'):
            p = os.path.join(dp, f)
            c = open(p, encoding='utf-8').read()
            if 'MultipartFile' in c:
                rel = os.path.relpath(p, mono)
                print('  ', rel)
                for line in c.split(chr(10)):
                    if 'RequestMapping' in line or 'PostMapping' in line:
                        print('     ', line.strip())
