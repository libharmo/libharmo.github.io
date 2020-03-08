import json
import os
import shutil
with open('lib-version-all.json','r') as f:
    j = json.load(f)
    li = []
    cnt = 0
    cnt2 = 0
    for k in j:
        data = k.split('__fdse__')
        li.append(data[1])
        versions = j[k]
        cnt+=len(versions)
        for v in versions:
            s = data[1]+"-"+v+".jar"
            if not os.path.exists('J:\\292projs_libs_all\\'+s):
                # if '$' in s or "SNAPSHOT" in s:
                    # cnt2+=1
                    # continue
                print(s)
                cnt2+=1
    print(cnt)
    print(cnt2)

    # path = 'J:\\libs\\lib_5000Plus'
    # files = os.listdir(path)
    # for file in files:
    #     for i in li:
    #         if file.startswith(i):
    #             if os.path.exists('J:\\292projs_libs_all\\'+file):
    #                 break
    #             shutil.copy(path +'\\'+file,'J:\\292projs_libs_all\\'+file)
    #             break