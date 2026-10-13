# -*- coding: utf-8 -*-
"""查询各服务 Controller 路由前缀"""
import io, sys, os, re
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
root = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
for svc in ['uaa-service','course-service','selection-score-service','attendance-service','warning-service','ai-service','analysis-service','teaching-service']:
    base = os.path.join(root, svc, 'src', 'main', 'java')
    prefixes = set()
    files = []
    for dp, dn, fn in os.walk(base):
        for f in fn:
            if f.endswith('Controller.java') and 'internal' not in dp:
                files.append(os.path.join(dp, f))
    for p in files:
        c = open(p, encoding='utf-8').read()
        for m in re.finditer(r'@RequestMapping\("([^"]+)"\)', c):
            prefixes.add(m.group(1))
    print('%s controllers=%d prefixes=%s' % (svc, len(files), sorted(prefixes)))
