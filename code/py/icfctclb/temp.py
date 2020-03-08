import os
import json

with open('mv_projs.txt','r') as f:
    j = json.load(f)

with open('200_plus_with_type.txt','r') as f:
    j2 = json.load(f)

def find(d,i):
    for item in d:
        if item['id'] == i:
            if 'gradle' in item['type']:
                return None
            else:
                return item['name']
    return None

cnt=0
print(len(j))
result = []
for i in j:
    name  =find(j2,i)
    if name !=None:
        a = {}
        a['id'] = i
        a['local_addr'] =  name
        result.append(a)
        cnt+=1
print(cnt)
with open('pomtreeinput.json','w') as f:
    json.dump(result,f,indent=4)