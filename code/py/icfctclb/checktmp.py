import os

import json
# with open('modules-id.json','r') as f:
#     j = json.load(f)
#     print(len(j))
with open('projs12.7.json','r') as f:
    d = json.load(f)
newDict = {}
with open('modules_maven.json','r') as f:
    j = json.load(f)
    print(len(j))
    for item in j:
        data = item.split('__fdse__')
        url  = 'https://github.com/'+data[0]+'/'+data[1]
        projid = None
        for p in d:
            if p['url'] == url:
                projid = p['id']
        projid = str(projid)
        newDict[projid] = j[item]
with open('modules_maven-id.json','w') as f:
    json.dump(newDict,f,indent=4)
    # cnt =0
    # for item in j:
    #     if len(j[item])<=1:
    #         cnt+=1
    # print(cnt)
