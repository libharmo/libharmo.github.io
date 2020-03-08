# coding=utf-8
import os
import json
import DepGraph
import trimPathUtil
import shutil
import harmony
import subSortOutDependency
import copy

def detectCornerCase(unnified,falseUnified):
    # -----------------graph依旧有两个的情况
    print('Graph2 ic:----------------------')
    for proj in unnified:
        for lib in unnified[proj]:
            if len(unnified[proj][lib]) != 1:
                print(proj+"++++++"+lib)
    #             tmp = unnified[proj][lib]
    #             for k in tmp:
    #                 print('key:'+str(k))
    #             print('2')
    #             print(proj)
    #             print(lib)
    print('Graph2 fc:----------------------')
    for proj in falseUnified:
        for lib in falseUnified[proj]:
            if len(falseUnified[proj][lib]) != 1:
                print(proj+"++++++"+lib)
    #             tmp = falseUnified[proj][lib]
                # print('2')
                # print(proj)
                # print(lib)


def popVersionSameLib(metaMultipleUsedLib):
    metaMultipleUsedLibVersionDiffer = copy.deepcopy(metaMultipleUsedLib)
    # metaSameVersion = {}
    removalList = []
    for proj in metaMultipleUsedLib:
        libUnique = {}
        for pom in metaMultipleUsedLib[proj]:
            for lib in metaMultipleUsedLib[proj][pom]:
                if not lib in libUnique:
                    libUnique[lib] = []
                libUnique[lib].append(pom)
        for lib in libUnique:
            verList = []
            isDup = False
            pomList = libUnique[lib]
            for pom in pomList:
                tup = metaMultipleUsedLib[proj][pom][lib]
                ver = tup[0]
                resolvedVersion = tup[1]
                if resolvedVersion in verList:
                    isDup = True
                else:
                    verList.append(resolvedVersion)
            if isDup == True and len(verList) == 1:
                for pom in pomList:
                    removalList.append((proj,pom,lib))

    metaSameVersion = subSortOutDependency.trimMapUniveral(metaMultipleUsedLibVersionDiffer,removalList)
    return metaMultipleUsedLibVersionDiffer,metaSameVersion



def popLibUsedOnce(meta):
    cnt = 0
    metaMultipleUsedLib = copy.deepcopy(meta)
    removalList = []
    for proj in meta:
        libUnique = {}
        for pom in meta[proj]:
            for lib in meta[proj][pom]:
                if not lib in libUnique:
                    libUnique[lib] = []
                libUnique[lib].append(pom)
        for lib in libUnique:
            if len(libUnique[lib]) == 1:
                # remove pom dict
                cnt+=1
                removalList.append((proj,libUnique[lib][0],lib))

    removalMap = subSortOutDependency.trimMapUniveral(metaMultipleUsedLib,removalList)
    print('Lib uniques uniques-----------:' +str(cnt))
    return metaMultipleUsedLib,removalMap

# 不属于module 的pom删除
def trimPoms(root,m_dict):
    with open(root+'/modules_maven-id.json','r') as f:
        realPom = json.load(f)
    projList = set()
    posli = {}
    for proj in m_dict:
        for lib in m_dict[proj]:
            for subg in m_dict[proj][lib]:
                cnt = 0
                for item in m_dict[proj][lib][subg]:
                    usePos = item['usePostion']
                    if usePos.endswith('pom.xml'):
                        usePos = usePos[0:-8]
                        if not proj in realPom:
                            projList.add(proj)
                            continue
                        if not usePos in realPom[proj]:
                            FDSE = "__FDSE__"
                            key = proj+FDSE+lib+FDSE+str(subg)
                            if not key in posli:
                                posli[key] = []
                            posli[key].append(cnt)
                    cnt+=1
    print('Trim-------------')
    print(len(projList))
    print(len(posli))
    for projId in projList:
        m_dict.pop(projId)
    for entry in posli:
        data = entry.split(FDSE)
        tempLi = posli[entry]
        tempLi.reverse()
        for index in tempLi:
            m_dict[data[0]][data[1]][int(data[2])].pop(index)
    popList = []
    for p in m_dict:
        for lib in m_dict[p]:
            for subg in m_dict[p][lib]:
                if len(m_dict[p][lib][subg]) <=1:
                    popList.append((p,lib,subg))
    for item in popList:
        m_dict[item[0]][item[1]].pop(item[2])
    popList = []
    for p in m_dict:
        for lib in m_dict[p]:
            if len(m_dict[p][lib]) == 0:
                popList.append((p,lib))
    for item in popList:
        m_dict[item[0]].pop(item[1])
    popList = []
    for p in m_dict:
        if len(m_dict[p]) == 0:
            popList.append(p)
    # print('---------Pop proj:')
    for item in popList:
        # print(item)
        m_dict.pop(item)
    # print('---------proj list')
    # for item in projList:
    #     print(item)
    # print('---------Pop end')

def checkIsModule(pomName,libProj,realPom):
    # 1. not consider module
    if not libProj[0:-4] in realPom:
        return False
    return True
    # 2. consider module
    # if pomName.endswith('pom.xml'):
    #     pomName2 = pomName[0:-8]
    #     if not libProj[0:-4] in realPom:
    #         return False
    #     if not pomName2 in realPom[libProj[0:-4]]:
    #         return False
    # return True    

def mvPreprocess(root,realPom,phase):
    if phase == 1:
        subdir = '1.data overview'
    else:
        subdir = '2.check'
    dependencyLibPath = root+"/"+subdir+"/taggedlibversion"
    dependencyTreePath = root+"/"+subdir+"/tree"
    libJsons = os.listdir(dependencyLibPath)
    meta = {}
    with open('icfc/sl.json','r') as f:
        slAll = json.load(f)
    with open('pom_count443-id.json','r') as f:
        modulesize = json.load(f)
    sl = {}
    if phase == 1:
        for proj in modulesize:
            if proj in slAll:
                sl[proj] = slAll[proj]
    print("Init Proj: "+str(len(libJsons)))
    graphDict = {}
    for libProj in libJsons:
        # print(libProj)
        meta[libProj[0:-4]] = {}
        dependencyName = libProj[0:-4]+".json"
        libPath = dependencyLibPath+"/"+libProj
        dependencyPath = dependencyTreePath+"/"+dependencyName
        projLibJson = subSortOutDependency.openJsonFile(libPath)
        dependencyTreeJson = subSortOutDependency.openJsonFile(dependencyPath)
        # gen tree
        graph = subSortOutDependency.genGraph(dependencyTreeJson)
        graphDict[libProj[0:-4]] = graph
        # graph.printGraph()
        for pomName in projLibJson:
            ffff = checkIsModule(pomName,libProj,realPom)
            if not ffff:
                continue
            meta[libProj[0:-4]][pomName] = {}
            libDict = projLibJson[pomName]
            for libName in libDict:
                kkkey = None
                versions = libDict[libName]
                if len(versions)==0:
                    continue
                subSortOutDependency.trimVersionPathList(versions)
                if len(versions) > 1:
                    can,kkkey = subSortOutDependency.chooseOne(pomName,versions,graph)
                    realVersion = can
                else:
                    for key in versions:
                        realVersion = versions[key]
                        kkkey = key
                if kkkey== "null":
                    # pass 解析出来为null的
                    continue
                meta[libProj[0:-4]][pomName][libName] = (realVersion,kkkey) # kkey keyversion
    # start
    # 删除了仅仅用了一处的lib
    metaMultipleUsedLib,metaUsedOnceLib = popLibUsedOnce(meta)
    sltemp = subSortOutDependency.transDictPOMLibToLibPOM(metaUsedOnceLib,graphDict,isSl=True)
    subSortOutDependency.putSlTempToSl(sltemp,sl,modulesize)
    print("Projs lib use 2+: "+str(len(metaMultipleUsedLib)))
    # pop 了version相同的情况
    metaMultipleUsedLibVersionDiffer,metaVersionSame = popVersionSameLib(metaMultipleUsedLib)
    print("Projs lib version differ: "+str(len(metaMultipleUsedLibVersionDiffer)))
    print("Projs lib version same: "+str(len(metaVersionSame)))
    ic = subSortOutDependency.transDictPOMLibToLibPOM(metaMultipleUsedLibVersionDiffer,graphDict)
    # # ----------------------删除版本解析不出来的
    subSortOutDependency.popInvalid(ic)
    # metaVersionSame  
    fcAndtc = subSortOutDependency.transDictPOMLibToLibPOM(metaVersionSame,graphDict)
    # --------------删除非多版本
    subGrpahNotMV = subSortOutDependency.deleteSubGraphWithOnePom(ic)
    # subGrpahNotMV
    subSortOutDependency.addDeletedSubGraphToFcTcOrSL(subGrpahNotMV,fcAndtc,sl)
    print("Same version: "+ str(len(fcAndtc)))
    fc = copy.deepcopy(fcAndtc)
    # tc,deletedEntry2 = subSortOutDependency.getDTC(fcAndtc)
    tc,deletedEntry2 = subSortOutDependency.getDTCNewLogic(fcAndtc)
    subSortOutDependency.trimMapUniveral(fc,deletedEntry2)
    return ic,fc,tc,sl

def writeToJson(root,ic,fc,tc,sl):
    with open(root+'/icfc/ic-8-21-pm.json','w') as f:
        json.dump(ic,f,indent=4)
    with open(root+'/icfc/fc-8-21-pm.json','w') as f:
        json.dump(fc,f,indent=4)
    with open(root+'/icfc/tc-8-21-pm.json','w') as f:
        json.dump(tc,f,indent=4)
    with open(root+'/icfc/sl-8-21-pm.json','w') as f:
        json.dump(sl,f,indent=4)
    # reportProjectSet = set()
    # 8-21 pm 重跑项目
    # for proj in ic:
    #     reportProjectSet.add(proj)
    # for proj in fc:
    #     reportProjectSet.add(proj)
    # li = list(reportProjectSet)
    
    # print("Merge ic and fc len:"+str(len(li)))
    # with open(root+'/icfc/report-project-8-20.json','w') as f:
    #     json.dump(li,f,indent=4)

def icfcSize(m_dict):
    cnt = 0 
    cnt2 = 0
    for proj in m_dict:
        cnt2+=1
        for lib in m_dict[proj]:
            cnt+=1
    return cnt2,cnt

def step1(phase):
    root = os.getcwd()
    with open(root+'/modules_maven-id.json','r') as f:
        realPom = json.load(f)
    ic,fc,tc,sl = mvPreprocess(root,realPom,phase)
    a,b = icfcSize(ic)
    print('ic.proj: '+str(a)+" ic.projlib: "+str(b))
    a,b = icfcSize(fc)
    print('fc.proj: '+str(a)+" fc.projlib: "+str(b))
    a,b = icfcSize(tc)
    print('tc.proj: '+str(a)+" tc.projlib: "+str(b))
    a,b = icfcSize(sl)
    print('sl.proj: '+str(a)+" sl.projlib: "+str(b))
    writeToJson(root,ic,fc,tc,sl)
    # remove unlinked modules
    # print("inconsistent len before trim pom:"+str(len(ic)))
    # print("false consistent len before trim pom:"+str(len(fc)))
    # trimPoms(root,ic)
    # trimPoms(root,fc)
    # detectCornerCase(ic,fc)
    # -------------------------