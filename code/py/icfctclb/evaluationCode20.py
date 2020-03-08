import json
import os

# 18是重跑后的数据

with open('icfc/ic-8-18.json','r') as f:
    ic = json.load(f)
with open('icfc/fc-8-18.json','r') as f:
    fc = json.load(f)
# with open('modules_maven-id.json','r') as f:
#     modulesize = json.load(f)
# with open('pom_count443-id.json','r') as f:
#     modulesize = json.load(f)
# with open('icfc/tc-8-18-pm.json','r') as f:
#     tc = json.load(f)
# with open('icfc/sl.json','r') as f:
#     sl = json.load(f)
with open('icfc/ic-8-20.json','r') as f:
    ic2 = json.load(f)
with open('icfc/fc-8-20.json','r') as f:
    fc2 = json.load(f)

projs = [85,349,865,1356,1659,3068,2057,68]

for p in projs:
    p = str(p)
    print("-+++++++++-------------------"+p)
    if p in ic:
        previc = ic[p]
    else:
        previc = []
    if p in ic2:
        curric = ic2[p]
    else:
        curric = []
    if p in fc:
        prevfc = fc[p]
    else:
        prevfc = []
    if p in fc2:
        currfc = fc2[p]
    else:
        currfc = []
    print('--------previc')
    print(len(previc))
    print('--------curric')
    print(len(curric))
    print('--------previc')
    print(len(prevfc))
    print('--------curric')
    print(len(currfc))
        


