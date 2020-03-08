import os
import json


def genTrimFile():
    with open('tongji_with_index-8-5.json','r') as f:
        index = json.load(f)
    with open('tongjiresult-8-6.json','r') as f:
        j = json.load(f)

    result = {}


    for proj in index:
        for lib in index[proj]:
            for subg in index[proj][lib]:
                flag = False
                if proj in j:
                    if lib in j[proj]:
                        if subg in j[proj][lib]:
                            flag = True
                            if proj not in result:
                                result[proj] = {}
                            if lib not in result[proj]:
                                result[proj][lib] = {}
                            if subg not in result[proj][lib]:
                                result[proj][lib][subg] = index[proj][lib][subg]
                if flag == False:
                    print('EDE')

    with open('tongji_with_index-8-5-trim.json','w') as f:
        json.dump(result,f,indent=4)




def 

