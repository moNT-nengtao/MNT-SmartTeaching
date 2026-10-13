# -*- coding: utf-8 -*-
import io, sys, os, glob
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
base = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
# 找出所有服务 resources/mapper 下的 UserMapper.xml
for x in glob.glob(base + r'\*-service\src\main\resources\mapper\UserMapper.xml'):
    svc = os.path.basename(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(x)))))
    # 路径: base\svc\src\main\resources\mapper\UserMapper.xml
    svc = x.split('smart-teaching-cloud\\')[1].split('\\')[0]
    if svc != 'uaa-service':
        os.remove(x)
        print('已删除:', os.path.relpath(x, base))
    else:
        print('保留(uaa):', os.path.relpath(x, base))
