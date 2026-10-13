# -*- coding: utf-8 -*-
"""去除所有 .java 文件开头的 UTF-8 BOM（javac 无法解析 BOM）"""
import io, sys, os
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
root = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
removed = 0
for dp, dn, fn in os.walk(root):
    if 'target' in dp or '.git' in dp:
        continue
    for f in fn:
        if f.endswith('.java'):
            p = os.path.join(dp, f)
            with open(p, 'rb') as fp:
                raw = fp.read()
            if raw.startswith(b'\xef\xbb\xbf'):
                with open(p, 'wb') as fp:
                    fp.write(raw[3:])
                removed += 1
                print('BOM removed:', os.path.relpath(p, root))
print('total removed:', removed)
