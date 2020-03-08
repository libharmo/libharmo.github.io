import os
import json

def icfcSize(m_dict):
    cnt = 0 
    cnt2 = 0
    for proj in m_dict:
        cnt2+=1
        for lib in m_dict[proj]:
            cnt+=1
    return cnt2,cnt

def projNametoId():
    with open('pom_count443.json','r') as f:
        j = json.load(f)
    with open('projs12.7.json','r') as f:
        ddd = json.load(f)
    newD = {}
    for proj in j:
        for item in ddd:
            url = item['url']
            data = url.split('/')
            projName = data[-2]+"__fdse__"+data[-1]
            if projName == proj:
                idd = item['id']
                newD[str(idd)] = j[proj]
    print(len(newD))
    with open('pom_count443-id.json','w') as f:
        json.dump(newD,f,indent=4)

# projNametoId()     

# def checkcheck():
#     with open('modules.json','r') as f:
#         orig = json.load(f)
#     print(len(orig))
# checkcheck()
# to bowen

# def two():
#     result = []
#     for proj in fc:
#         libNum = len(fc[proj])
#         if proj in ic:
#             libNum += len(ic[proj])
#         moduleNum = len(modulesize[proj])
#         tup = (proj,'fc',libNum,moduleNum)
#         result.append(tup)
#     for proj in ic:
#         libNum = len(ic[proj])
#         if proj in fc:
#             libNum += len(fc[proj])
#         moduleNum = len(modulesize[proj])
#         tup = (proj,'ic',libNum,moduleNum)
#         result.append(tup)  
#     with open('RQ-result/rq1-scatterdot.json','w') as f:
#         json.dump(result,f,indent=4)
# two()


def jiraissue():
    with open("projs12.7.json",'r') as f:
        d = json.load(f)
    result = {}
    for proj in ic:
        for item in d:
            if item['id'] == int(proj):
                url = item['url']
                result[proj] = url
    with open('jira-ic-url.json','w') as f:
        json.dump(result,f,indent=4)
# jiraissue()


def fig5a_Pie1(ic,fc,tc,sl):
    a,b = icfcSize(ic)
    print('ic.proj: '+str(a)+" ic.projlib: "+str(b))
    a,b = icfcSize(fc)
    print('fc.proj: '+str(a)+" fc.projlib: "+str(b))
    a,b = icfcSize(tc)
    print('tc.proj: '+str(a)+" tc.projlib: "+str(b))
    a,b = icfcSize(sl)
    print('sl.proj: '+str(a)+" sl.projlib: "+str(b))


def fig5b_Pie2(ic,fc,tc,sl):
    icProjs = set()
    fcProjs = set()
    tcProjs = set()
    allProjs = set()
    for p in ic:
        icProjs.add(p)
        allProjs.add(p)
    for p in fc:
        fcProjs.add(p)
        allProjs.add(p)
    for p in tc:
        tcProjs.add(p)
        allProjs.add(p)
    icandfcandtc = icProjs & fcProjs & tcProjs
    icandfc = icProjs & fcProjs - icandfcandtc
    icandtc = icProjs & tcProjs - icandfcandtc
    fcandtc = fcProjs & tcProjs - icandfcandtc
    onlyIc = icProjs - (fcProjs.union(tcProjs))
    onlyFc = fcProjs - (icProjs.union(tcProjs))
    onlyTc = tcProjs - (icProjs.union(fcProjs))

    print("Sum:"+str(len(allProjs)))
    print('icandfcandtc:'+ str(len(icandfcandtc)))
    print('icandfc:'+ str(len(icandfc)))
    print('icandtc:'+ str(len(icandtc)))
    print('fcandtc:'+ str(len(fcandtc)))
    print('onlyIc:'+ str(len(onlyIc)))
    print('onlyFc:'+ str(len(onlyFc)))
    print('onlyTc:'+ str(len(onlyTc)))


def figureX_PopularLib(ic,fc,tc):
    fcDict = {}
    icDict = {}
    fcCnt = 0
    icCnt = 0
    for proj in fc:
        for lib in fc[proj]:
            if not lib in fcDict:
                fcDict[lib] = 0
            fcDict[lib]+=1
            fcCnt+=1
    fcDictSorted = sorted(fcDict.items(), key=lambda d: d[1],reverse=True)

    for proj in ic:
        for lib in ic[proj]:
            if not lib in icDict:
                icDict[lib] = 0
            icDict[lib]+=1
            icCnt +=1

    icDictSorted = sorted(icDict.items(), key=lambda d: d[1],reverse=True)
    a = {}
    a['ic'] = icDictSorted
    a['fc'] = fcDictSorted
    print(fcCnt)
    print(icCnt)
    with open('RQ-result/meta-popular-fc-ic-lib-8-20.json','w') as f:
        json.dump(a,f,indent=4)


def figure9ab(ic,fc):
    mixed_fc = 0
    allex_fc = 0
    allim_fc = 0
    mixed_ic = 0
    allex_ic = 0
    allim_ic = 0
    for proj in fc:
        for lib in fc[proj]:
            l = len(fc[proj][lib])
            if l!=1:
                pass
                # print('ee')
                # continue
            s = set()
            for subg in fc[proj][lib]:
                for item in fc[proj][lib][subg]:
                    s.add(item['isProperty'])
            if len(s)!=1:
                mixed_fc +=1
            else:
                if True in s:
                    allim_fc+=1
                else:
                    allex_fc+=1
    for proj in ic:
        for lib in ic[proj]:
            l = len(ic[proj][lib])
            if l!=1:
                pass
                # print('ee')
                # continue
                #todo
            s = set()
            for subg in ic[proj][lib]:
                for item in ic[proj][lib][subg]:
                    s.add(item['isProperty'])
            if len(s)!=1:
                mixed_ic +=1
            else:
                if True in s:
                    allim_ic+=1
                else:
                    allex_ic+=1
    print("FC Total:"+str(mixed_fc+allex_fc+allim_fc))
    print("FC Mixed:"+str(mixed_fc))
    print("FC EX:"+str(allex_fc))
    print("FC IM:"+str(allim_fc))
    print("IC Total:"+str(mixed_ic+allex_ic+allim_ic))
    print("IC Mixed:"+str(mixed_ic))
    print("IC EX:"+str(allex_ic))
    print("IC IM:"+str(allim_ic))
    # FC Total:2195
    # FC Mixed:84
    # FC EX:2059
    # FC IM:52
    # IC Total:505
    # IC Mixed:115
    # IC EX:184
    # IC IM:206
    #------
    # FC Total:2576
    # FC Mixed:98
    # FC EX:2423
    # FC IM:55
    # IC Total:621
    # IC Mixed:131
    # IC EX:224
    # IC IM:266     
def subfour(data,proj,lib):
    tup = {}
    tup['project'] = proj    
    tup['lib'] = lib
    l = len(data)

    if l!=1:
        pass
        # print('ee')
    versions = set()
    propertyName = set()
    tup['module'] = 0
    for subg in data:
        for item in data[subg]:
            versions.add(item['resolved_version'])
            if item['isProperty'] == True and item['propertyName'] != None:
                propertyName.add(item['propertyPosition']+"__fdse__"+item['propertyName'])
        tup['module'] += len(data[subg])
    tup['distinct_version'] = len(versions)
    tup['distinct_property'] = len(propertyName)

    return tup


def fig8a(ic,fc,tc):
    result = []
    for proj in fc:
        for lib in fc[proj]:
            tup = subfour(fc[proj][lib],proj,lib)
            tup['type'] = 'fc'
            result.append(tup)
    for proj in ic:
        for lib in ic[proj]:
            tup = subfour(ic[proj][lib],proj,lib)
            tup['type'] = 'ic'
            result.append(tup)
    for proj in tc:
        for lib in tc[proj]:
            tup = {}
            tup['type'] = 'tc'
            tup['project'] = proj    
            tup['lib'] = lib
            tup['module'] = "?"
            result.append(tup)
    # with open('RQ-result/rq3-declarations-8-18.json','w') as f:
    with open('RQ-result/declarations-8-21.json','w') as f:
        json.dump(result,f,indent=4)

def fig8bDistinctVersion(ic):
    result = []
    for proj in ic:
        for lib in ic[proj]:
            tup = {}
            tup['type'] = 'ic'
            tup['project'] = proj    
            tup['lib'] = lib
            data = ic[proj][lib]
            versions = {}
            propertyName = set()
            tup['module'] = 0
            for subg in data:
                for item in data[subg]:
                    # versions.add(item['resolved_version'])
                    if item['resolved_version'] not in versions:
                        versions[item['resolved_version']] = 0
                    versions[item['resolved_version']]+=1
                    if item['isProperty'] == True and item['propertyName'] != None:
                        propertyName.add(item['propertyPosition']+"__fdse__"+item['propertyName'])
                tup['module'] += len(data[subg])
            if len(versions) == 2:
                tup['distinct_version'] = versions
                tup['distinct_property'] = len(propertyName)
                result.append(tup)
    with open('RQ-result/distinctversion-8-21.json','w') as f:
        json.dump(result,f,indent=4)
    cntt = 0
    cnttt = 0
    for item in result:
        if item['type'] == 'ic' and item['module']>=10 and len(item['distinct_version'])==2:
            cntt+=1
            ddd = item['distinct_version']
            m = []
            for key in ddd:
                m.append(ddd[key])
            m.sort()
            if m[0] == 1:
                cnttt+=1
    print(cnttt)
    print(cntt)


def fig7a7b_icfcpercentage(ic,fc,modulesize):
    dic = ic
    dfc = fc
    result = []
    for proj in dic:
        for lib in dic[proj]:
            entry = {}
            entry['type'] = 'ic'
            amoduleCnt = 0
            entry['module'] = modulesize[proj]
            for subg in dic[proj][lib]:
                amoduleCnt += len(dic[proj][lib][subg])
            entry['affected_module'] = amoduleCnt
            result.append(entry)
    for proj in dfc:
        for lib in dfc[proj]:
            entry = {}
            entry['type'] = 'fc'
            amoduleCnt = 0
            entry['module'] = modulesize[proj]
            for subg in dfc[proj][lib]:
                amoduleCnt += len(dfc[proj][lib][subg])
            entry['affected_module'] = amoduleCnt
            result.append(entry)
        
    with open('RQ-result/icfcpercentage-8-21.json','w') as f:
        json.dump(result,f,indent=4)


def overviewDataFor4types():
    # root = 'D:/MultiVersions/'
    root = ''
    # with open(root+'report-project-8-6.json','r') as f:
    #     j = json.load(f) 
    with open(root+'meta_projects-8-2.json','r') as f:
        j2 = json.load(f)
    modules = j2['module']
    print('Module:'+str(len(modules)))
    dic = ic
    dfc = fc
    with open(root+'d_single.json','r') as f:
        ds = json.load(f)
    
    result = {}
    result['ic'] = {}
    result['fc'] = {}
    result['tc'] = {}
    result['s'] = {}
    temp = set()
    temp2 = set()
    for proj in dic:
        if not proj in result['ic']:
            result['ic'][proj] = []
        for lib in dic[proj]:
            result['ic'][proj].append(lib)
        temp.add(proj)
        temp2.add(proj)
    for proj in dfc:
        if not proj in result['fc']:
            result['fc'][proj] = []
        for lib in dfc[proj]:
            result['fc'][proj].append(lib)
        temp.add(proj)
        temp2.add(proj)
    for proj in dtc:
        if proj not in modules:
            continue
        if not proj in result['tc']:
            result['tc'][proj] = []
        for lib in dtc[proj]:
            result['tc'][proj].append(lib)
        temp2.add(proj)
    for proj in ds:
        if not proj in modules:
            continue
        # if proj not in temp:
            # continue
        if not proj in result['s']:
            result['s'][proj] = []
        s = set()
        for lib in ds[proj]:
            s.add(lib)
        temp2.add(proj)
        result['s'][proj] = list(s)
    printd(result['ic'])
    printd(result['fc'])
    printd(result['tc'])
    printd(result['s'])
    print('eee:'+str(len(temp)))
    print('eee:'+str(len(temp2)))
    with open('RQ-result/330ProjectICFCTCSOverview-8-18.json','w') as f:
        json.dump(result,f,indent=4)
    with open('RQ-result/330ProjectICFCTCSOverview-8-18.json','r') as f:
        j = json.load(f)
        d1 = j['ic']
        d2 = j['fc']
        d3 = j['tc']
        d4 = j['s']
        sd = set()
        for proj in d1:
            sd.add(str(proj))
        for proj in d2:
            sd.add(str(proj))
        for proj in d3:
            sd.add(str(proj))
        for proj in d4:
            sd.add(str(proj))
        print(len(sd))
# overviewDataFor4types()

def getIndex(moduleNumber):
    index = moduleNumber//5
    if index>=6:
        index = 6
    return index
    # if moduleNumber>=1 and moduleNumber<=10:
    #     return 0
    # if moduleNumber>=11 and moduleNumber<=20:
    #     return 1
    # if moduleNumber>=21 and moduleNumber<=30:
    #     return 2
    # if moduleNumber>=31 and moduleNumber<=40:
    #     return 3
    # if moduleNumber>=41 and moduleNumber<=50:
    #     return 4
    # if moduleNumber>=51:
    #     return 5



def overviewModuleDataFor4types(ic,fc,tc,sl,modulesize):
    root = ''
    dic = ic
    dfc = fc
    ds = sl
    result = {}
    result['ic'] = {}
    result['fc'] = {}
    result['tc'] = {}
    result['s'] = {}
    for proj in dic:
        if not proj in result['ic']:
            if proj in modulesize:
                result['ic'][proj] = modulesize[proj]
    for proj in dfc:
        if not proj in result['fc']:
            if proj in modulesize:
                result['fc'][proj] = modulesize[proj]
    for proj in tc:
        if not proj in result['tc']:
            if proj in modulesize:
                result['tc'][proj] = modulesize[proj]
    for proj in ds:
        if not proj in modulesize:
            continue
        # if proj not in dic or proj not in dfc:
        #     continue  
        if proj in modulesize:
            result['s'][proj] = modulesize[proj]
        
    print(len(result['ic']))
    print(len(result['fc']))
    print(len(result['tc']))
    print(len(result['s']))
    with open('RQ-result/ProjectICFCTCModuleSize-8-21.json','w') as f:
        json.dump(result,f,indent=4)  
    curve = {}
    for i in range(0,7):
        curve[str(i)] = {}
        curve[str(i)]['ic'] = 0
        curve[str(i)]['tc'] = 0
        curve[str(i)]['fc'] = 0
        curve[str(i)]['sl'] = 0
    for proj in ic:
        for lib in ic[proj]:
            # moduleNumber = 0
            # for subg in ic[proj][lib]:
            #     moduleNumber += len(ic[proj][lib][subg])
            moduleNumber = modulesize[proj]
            index = getIndex(moduleNumber)
            curve[str(index)]['ic']+=1
    for proj in fc:
        for lib in fc[proj]:
            # moduleNumber = 0
            # for subg in fc[proj][lib]:
            #     moduleNumber += len(fc[proj][lib][subg])
            moduleNumber = modulesize[proj]
            index = getIndex(moduleNumber)
            curve[str(index)]['fc']+=1
    for proj in tc:
        for lib in tc[proj]:
            # moduleNumber = 0
            # for subg in tc[proj][lib]:
            #     moduleNumber += len(tc[proj][lib][subg])
            moduleNumber = modulesize[proj]
            if moduleNumber<=1:
                continue
            index = getIndex(moduleNumber)
            curve[str(index)]['tc']+=1
    for proj in sl:
        if not proj in modulesize:
            continue
        for lib in sl[proj]:
            moduleNumber = modulesize[proj]
            index = getIndex(moduleNumber)
            curve[str(index)]['sl']+=1

    with open('RQ-result/curve-dis-8-21.json','w') as f:
        json.dump(curve,f,indent=4)