import os
import json
import shutil

def onlyCopyProjFromMvInfo():
    with open('mv_info_maven293.json','r') as f:
        j = json.load(f)
        print(len(j))
        for k in j:
            if os.path.exists('C:/Users/huangkaifeng/Desktop/tree/'+k+'.json'):
                shutil.copy('C:/Users/huangkaifeng/Desktop/tree/'+k+'.json','C:/Users/huangkaifeng/Desktop/tree2/'+k+'.json')
            else:
                print(k)
            
onlyCopyProjFromMvInfo()