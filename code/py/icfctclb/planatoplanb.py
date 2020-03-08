import os

import json

p = set()
aa = {}
with open('tongjiresult-7-17.json','r') as f:
    j = json.load(f)
    for i in j:
        p.add(i)
with open('tongji-un-unified-7-17.json','r') as f:
    j = json.load(f)
    for i in j:
        p.add(i)
with open('projs12.7.json','r') as f:
    proj = json.load(f)
with open('modules.json','r') as f:
    modules = json.load(f) 
    print(len(modules))
    print('---')
for mo in modules:
    data = mo.split("__fdse__")
    url = "https://github.com/"+data[0]+'/'+data[1]
    idd = None
    for ppp in proj:
        if ppp['url'] == url:
            idd  = ppp['id']
    if modules[mo] == None or len(modules[mo])==0:
        continue
    aa[str(idd)] = modules[mo]
with open('modules-id.json','w') as f:
    json.dump(aa,f,indent=4)
for ii in aa:
    if not ii in p:
        print('---'+str(ii))
print(len(aa))
print(len(p))