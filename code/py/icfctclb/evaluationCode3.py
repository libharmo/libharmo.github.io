import os
import json

root = os.getcwd()
print(root)
with open(root+'/ic-8-18.json','r') as f:
    fc = json.load(f)
with open(root+ '/fc-8-18.json','r') as f:
    ic = json.load(f)
with open(root+ '/modules_maven-id.json','r') as f:
    modulesize = json.load(f)

def checkDistinctVersions():
    d = {}
    for proj in ic:
        for lib in ic[proj]:
            aaa = set()
            for subg in ic[proj][lib]:
                for item in ic[proj][lib][subg]:
                    aaa.add(item['resolved_version'])
            if len(aaa) not in d:
                d[len(aaa)] = 0
            d[len(aaa)] +=1
            if len(aaa) ==4 :
                print(aaa)
    print(d)

def disVariable():
    ddd = {}
    for proj in ic:
        for lib in ic[proj]:
            aaa = {}
            for subg in ic[proj][lib]:
                for item in ic[proj][lib][subg]:
                    if item['isProperty'] == True:
                        if not item['propertyName'] in aaa:
                            aaa[item['propertyName']] = []
                        aaa[item['propertyName']].append(item['resolved_version']+" "+item['usePostion'])
            l  = len(aaa)
            if l>=4:
                flag = True
                for ite in aaa:
                    if 'tomcat' in ite:
                        flag = True
                if  flag:
                    print('---------------------------')
                    print(proj+" "+lib)
                    for key in aaa:
                        print('---------------------------__')
                        print(key)
                        print(aaa[key])
            if not l in ddd:
                ddd[l] = 0
            ddd[l]+=1
    # ddd2 = {}
    print(ddd)
    # for proj in fc:
    #     for lib in fc[proj]:
    #         aaa = set()
    #         for subg in fc[proj][lib]:
                
    #             for item in fc[proj][lib][subg]:
    #                 if item['isProperty'] == True:
    #                     aaa.add(item['propertyName'])
    #         l  = len(aaa)
    #         if l>=3:
    #             print(aaa)
    #         if not l in ddd2:
    #             ddd2[l] = 0
    #         ddd2[l]+=1
    
    # print(ddd2)
# disVariable() 
