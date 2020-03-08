import os
import json
import trimPathUtil
import harmony
import DepGraph

WY_RemotePomLocation = 'E:/data/multiversion/pom/'
KF_RemotePomLocation = 'D:/Workspace/WyRepos/pom/'
WY_RemotePomLocation2 = 'C:/data/pom/'

def putSlTempToSl(sltemp,sl,modulesize):
    for proj in sltemp:
        if not proj in modulesize:
            continue
        for lib in sltemp[proj]:
            if not proj in sl:
                sl[proj] = {}
            if not lib in sl[proj]:
                sl[proj][lib] = sltemp[proj][lib]

def addDeletedSubGraphToFcTcOrSL(subGrpahNotMV,fcAndtc,sl):
    if subGrpahNotMV == None:
        return
    for proj in subGrpahNotMV:
        for lib in subGrpahNotMV[proj]:
            if proj == '349' and lib == 'org.languagetool__fdse__language-all__fdse__jar':
                print('a')
            itemCnt = 0                
            for subg in subGrpahNotMV[proj][lib]:
                itemCnt += len(subGrpahNotMV[proj][lib][subg])
            if itemCnt ==1:
                for subg in subGrpahNotMV[proj][lib]:
                    if not proj in sl:
                        sl[proj] = {}
                    if not lib in sl:
                        sl[proj][lib] = {}
                    if not subg in sl[proj][lib]:
                        sl[proj][lib][subg] = subGrpahNotMV[proj][lib][subg]
            else:
                entry1 = fcAndtc['349']
                for subg in subGrpahNotMV[proj][lib]:
                    if not proj in fcAndtc:
                        fcAndtc[proj] = {}
                    if not lib in fcAndtc[proj]:
                        fcAndtc[proj][lib] = {}
                    if not subg in fcAndtc[proj][lib]:
                        fcAndtc[proj][lib][subg] = subGrpahNotMV[proj][lib][subg]

def deleteSubGraphWithOnePom(ic):
    deletedEntry = []
    for proj in ic:
        for lib in ic[proj]:
            deletedSubG = []
            for subG in ic[proj][lib]:
                rawVersionSet = set()
                propertyValueSet = set()
                resovledVersionSet = set()
                for item in ic[proj][lib][subG]:
                    rawVersionSet.add(item['rawVersion'])
                    if item['propertyValue'] !=None:
                        propertyValueSet.add(item['propertyValue'])
                    resovledVersionSet.add(item['resolved_version'])
                # 增加了sub graph之后，增加了sub graph version只有一个的情况
                if len(propertyValueSet)<=1 and len(rawVersionSet)<=1:
                    deletedEntry.append((proj,lib,subG))
                    deletedSubG.append(subG)
            for subG in ic[proj][lib]:
                if subG in deletedSubG:
                    continue
                resovledVersionSet = set()
                for item in ic[proj][lib][subG]:
                    resovledVersionSet.add(item['resolved_version'])
                if len(resovledVersionSet) ==1:
                    print('-------ddjdjsss----------------')

    subGrpahNotMV = trimMapUniveral(ic,deletedEntry)
    print("Proj deleting sub graph not mv: "+str(len(ic)))
    return subGrpahNotMV

def transDictPOMLibToLibPOM(m_dict,graphDict,isSl=None):
    newDict = {}
    for proj in m_dict:
        libResultDict = {}
        libResultNewDict = {}
        for pom in m_dict[proj]:
            for lib in m_dict[proj][pom]:
                if not lib in libResultDict:
                    libResultDict[lib] = []
                data = m_dict[proj][pom][lib]
                realVersion = data[0]
                resolvedVersion = data[1]
                libResultDict[lib].append((pom,realVersion,resolvedVersion))
        for lib in libResultDict:
            if isSl == True:
                libResultNewDict[lib] = libResultDict[lib]
                continue
            result = genMultiVersionType(lib,libResultDict[lib],graphDict[proj])
            libResultNewDict[lib] = result
        newDict[proj] = libResultNewDict
    return newDict


def popInvalid(ic):
    if '2735' in ic:
        ic.pop('2735')
    if '130' in ic:
        ic.pop('130')
    if '35' in ic:
        ic.pop('35')
    if '5027' in ic:
        ic.pop('5027')    
    if '143' in ic:
        ic.pop('143')
    if '1265' in ic:
        ic.pop('1265')


def getDTCNewLogic(metaVersionSameTotal):
    tcEntry = []    
    for proj in metaVersionSameTotal:
        for lib in metaVersionSameTotal[proj]:
            if proj == '240' and lib == 'com.jayway.awaitility__fdse__awaitility__fdse__jar':
                print('sss')
            if len(metaVersionSameTotal[proj][lib])>=2:
                # 2 more sub graph
                continue
            for subg in metaVersionSameTotal[proj][lib]:
                li = metaVersionSameTotal[proj][lib][subg]
                propertyName = set() # isProperty propertyName
                propertyPos = set()
                versionPos = set()
                ifExistIsPropertyFalse = False
                for item in li:
                    if ifExistIsPropertyFalse == False and item['isProperty'] == False:
                        ifExistIsPropertyFalse = True
                    propertyName.add(item['propertyName'])   
                    propertyPos.add(item['propertyPosition'])   
                    versionPos.add(item['versionPosition'])   
                if ifExistIsPropertyFalse:
                    if len(versionPos) == 1:
                        tcEntry.append((proj,lib,subg))
                        continue
                if len(propertyPos) == 1 and len(propertyName) ==1:
                    tcEntry.append((proj,lib,subg))
                    continue
                if len(versionPos) == 1 and len(propertyName) ==1 and len(propertyPos)!=1:
                    # *******
                    tcEntry.append((proj,lib,subg))
                    continue
                if len(versionPos) ==1:
                    print('sss----------------__++++++++++++++++'+str(len(propertyPos))+"   "+str(len(propertyName)))
    Dtc = {}
    for item in tcEntry:
        proj = item[0]
        lib = item[1]
        subg = item[2]
        if not proj in Dtc:
            Dtc[proj] = {}
        if not lib in Dtc[proj]:
            Dtc[proj][lib] = {}
        Dtc[proj][lib][subg] = metaVersionSameTotal[proj][lib][subg]

    return Dtc,tcEntry


def getDTC(metaVersionSameTotal):
    deletedEntry2 = []    
    for proj in metaVersionSameTotal:
        for lib in metaVersionSameTotal[proj]:
            for subg in metaVersionSameTotal[proj][lib]:
                li = metaVersionSameTotal[proj][lib][subg]
                propertyName = set() # isProperty propertyName
                ifExistIsPropertyFalse = False
                for item in li:
                    if item['isProperty'] == False:
                        ifExistIsPropertyFalse = True
                    propertyName.add(item['propertyName'])   
                if len(item)<=1:
                    deletedEntry2.append((proj,lib,subg))
                    continue
                if ifExistIsPropertyFalse:
                    continue
                if len(propertyName)>=2:
                    continue
                deletedEntry2.append((proj,lib,subg))
    Dtc = {}
    for item in deletedEntry2:
        proj = item[0]
        lib = item[1]
        subg = item[2]
        if not proj in Dtc:
            Dtc[proj] = {}
        if not lib in Dtc[proj]:
            Dtc[proj][lib] = {}
        Dtc[proj][lib][subg] = metaVersionSameTotal[proj][lib][subg]
    cntt = 0
    for proj in Dtc:
        for lib in Dtc[proj]:
            cnt = 0
            for subg in Dtc[proj][lib]:
                for item in Dtc[proj][lib][subg]:
                    cnt+=1
            if cnt<=1:
                cntt+=1
    print(cntt)
    return Dtc,deletedEntry2

def removePathPrefix(path):
    if path == None:
        return None
    global WY_RemotePomLocation
    global KF_RemotePomLocation
    global WY_RemotePomLocation2
    newPath = path
    if newPath.startswith(WY_RemotePomLocation):
        newPath = newPath.replace(WY_RemotePomLocation,'')
    if newPath.startswith(WY_RemotePomLocation2):
        newPath = newPath.replace(WY_RemotePomLocation2,'')
    if newPath.startswith(KF_RemotePomLocation):
        newPath = newPath.replace(KF_RemotePomLocation,'')
    return newPath

def trimMap(m):
    kList = []
    for k in m:
        if len(m[k])<=1:
            kList.append(k)
    for k in kList:
        m.pop(k)
    return m

def trimMapKey0(m):
    kList = []
    for k in m:
        if len(m[k]) == 0:
            kList.append(k)
    for k in kList:
        m.pop(k)
    return m


def trimMapUniveral(m,removalList):
    if len(removalList) ==0:
        return None
    removalMap = {}
    for item in removalList:
        k1 = item[0]
        k2 = item[1]
        k3 = item[2]
        popData = m[k1][k2].pop(k3)
        if not k1 in removalMap:
            removalMap[k1] = {}
        if not k2 in removalMap[k1]:
            removalMap[k1][k2] = {}
        removalMap[k1][k2][k3] = popData
    removalList2 = []
    for k1 in m:
        for k2 in m[k1]:
            if len(m[k1][k2]) ==0:
                removalList2.append((k1,k2))
    removalList3 = []
    for item in removalList2:
        m[item[0]].pop(item[1])
    for k in m:
        if len(m[k])==0:
            removalList3.append(k)
    for item in removalList3:
        m.pop(item)
    
    return removalMap

def openJsonFile(path):
    with open(path,'r') as f:
        j = json.load(f)
    return j

    
def keysToStr(keys):
    a = []
    for k in keys:
        a.append(k)
    a.sort()
    res = ""
    for k in a:
        res+=str(k)+" "
    return res

def trimVersionPathList(versions):
    for v in versions:
        trimVersionPath(versions[v])
    return versions


def trimVersionPath(v):
    versionPosition = v['versionPosition']
    if versionPosition != None:
        versionPositionNew = trimPathUtil.trimPath(versionPosition)
        v['versionPosition'] = versionPositionNew

    propertyPosition = v['propertyPosition']
    if propertyPosition != None:
        propertyPositionNew = trimPathUtil.trimPath(propertyPosition)
        v['propertyPosition'] = propertyPositionNew

    declarePosition = v['declarePosition']
    if declarePosition != None:
        declarePositionNew = trimPathUtil.trimPath(declarePosition)
        v['declarePosition'] = declarePositionNew


def checkNodeIfIsExist(graph,label):
    label = trimPathUtil.trimPath(label)
    label2 = removePathPrefix(label)
    f = graph.isNodeExist(label2)
    # if f == False:
        # print(label)
    return f

def genGraph(depends):
    li = depends['pom']
    relations = depends['relation']
    G = DepGraph.DepGraph()
    mMap = {}
    for pom in li:
        data = pom.split("__fdse__")
        tpom = trimPathUtil.trimPath(data[0])
        tpom = removePathPrefix(tpom)
        # if tpom.startswith('D:/Workspace/WyRepos/pom/'):
            # tpom = tpom.replace("D:/Workspace/WyRepos/pom/","")
        mMap[tpom] = data[1]
    for k in mMap:
        G.addNode(k,mMap[k])
    for relation in relations:
        a = trimPathUtil.trimPath(relation['A'])
        parent = trimPathUtil.trimPath(relation['parent'])
        # if a.startswith('D:/Workspace/WyRepos/pom/'):
            # a = a.replace('D:/Workspace/WyRepos/pom/','')
        a = removePathPrefix(a)
        # if parent.startswith('D:/Workspace/WyRepos/pom/'):
            # parent = parent.replace('D:/Workspace/WyRepos/pom/','')
        parent = removePathPrefix(parent)
        G.addEdge(a,parent)
    G.clustering()
    return G

def chooseOne(useLocation,versions,graph):
    # print('check')
    global WY_RemotePomLocation
    global WY_RemotePomLocation2
    inner = []
    outer = []
    if len(versions)==0:
        return None,None
    for v in versions:
        d = versions[v]
        versionPosition = d['versionPosition']
        declarePosition = d['declarePosition']
        f1 = checkNodeIfIsExist(graph,versionPosition)
        f2 = checkNodeIfIsExist(graph,declarePosition)
        if f1 == False or f2 == False:
            continue
        if versionPosition.startswith(WY_RemotePomLocation):
            outer.append(v)
        elif versionPosition.startswith(WY_RemotePomLocation2):
            outer.append(v)
        else:
            inner.append(v) 
    if len(inner) == 1:
        return versions[inner[0]],inner[0]
    tempList = None
    if len(inner)!=0:
        tempList = inner
    else:
        tempList = outer
    candidate = None
    kkey = None
    step = 10000000
    for v in tempList:
        d = versions[v]
        # print(d)
        versionPosition = d['versionPosition'] 
        versionPosition = trimPathUtil.trimPath(versionPosition)
        if versionPosition.startswith(WY_RemotePomLocation):
            versionPosition = versionPosition.replace(WY_RemotePomLocation,'')
        if versionPosition.startswith(WY_RemotePomLocation2):
            versionPosition = versionPosition.replace(WY_RemotePomLocation2,'')
        tempStep = graph.step(useLocation,versionPosition)
        if tempStep <step and tempStep!=-1:
            step = tempStep
            candidate = d
            kkey = v
    if candidate == None:
        print('err')
        return None,None
    else:
        # 连通
        return candidate,kkey


def genMultiVersionType(libName,mList,graph):
    # graph.clearLibFlagNum()
    result = {}
    for candidate in mList:
        flagNum = 0   
        useLoc = candidate[0]
        version = candidate[1]
        resolvedVersion = candidate[2]
        version['resolved_version'] = resolvedVersion
        declarePosition = version['declarePosition']
        declarePosition = removePathPrefix(declarePosition)
        # if declarePosition.startswith('E:/data/multiversion/pom/'):
            # declarePosition = declarePosition.replace('E:/data/multiversion/pom/','')
        version['declarePosition'] = declarePosition
        defineVersionPosition = version['versionPosition']
        defineVersionPosition = removePathPrefix(defineVersionPosition)
        # if defineVersionPosition.startswith('E:/data/multiversion/pom/'):
            # defineVersionPosition = defineVersionPosition.replace('E:/data/multiversion/pom/','')
        version['versionPosition'] = defineVersionPosition
        propertyPosition = version['propertyPosition']
        # if propertyPosition !=None and propertyPosition.startswith('E:/data/multiversion/pom/'):
            # propertyPosition = propertyPosition.replace('E:/data/multiversion/pom/','')
        propertyPosition = removePathPrefix(propertyPosition)
        version['propertyPosition'] = propertyPosition
        node = graph.getNode(useLoc)
        if "local" in  node.pomsource:
            num = 1<<6
            flagNum+=num
        else:
            print('???:' + node.pomsource)
        node = graph.getNode(declarePosition)
        # if "local" in node.pomsource:
        #     num = 1<<5
        #     flagNum+=num
        # if "local" in node.pomsource:
        #     num = 1<<4
        #     flagNum+=num
        # if useLoc == declarePosition:
        #     num = 1<<3
        #     flagNum+=num
        # if declarePosition == defineVersionPosition:
        #     num = 1<<2
        #     flagNum+=num
        # if useLoc == defineVersionPosition:
        #     num = 1<<1
        #     flagNum+=num
        # if version['isProperty'] == True:
        #     flagNum+=1
        index  = graph.getIndexOfSubGraphByNodeLabel(useLoc)
        # version['flagNumber'] = flagNum
        version['subGraphIndex'] = index
        version['usePostion'] = useLoc
        if index not in result:
            result[index] = []
        result[index].append(version)
        # print((flagNum,index,useLoc,declarePosition,definePosition,propertyPosition))
    return result
def toPathList(li):
    #todo
    flag = False
    path = set()
    for i in li:
        if i['isProperty'] == True:
            if i['propertyPosition'] !=None:
                path.add(i['propertyPosition'])
            else:
                # print('a')
                flag = True
    return list(path),flag

def separate(tongji):
    result = {}
    for proj in tongji:
        libDict = tongji[proj]
        for lib in libDict:
            d = libDict[lib]
            for subG in d:
                li = d[subG]
                typeSet =  set()
                for i in li:
                    typeSet.add(i['flagNumber'])
                typeList = list(typeSet)
                typeList.sort()
                key = ""
                for k in typeList:
                    key += str(k)+" "
                key = key.strip()
                if not key in result:
                    result[key] = {}
                if not proj in result[key]:
                    result[key][proj] = {}
                if not lib in result[key][proj]:
                    result[key][proj][lib] = {}
                result[key][proj][lib][subG] = li
    for patt in result:
        with open('D:\\MultiVersions\\resultbypatterns\\'+patt+".json",'w') as f:
            json.dump(result[patt],f,indent=4)

# def printResult(result):
#     path = 'D:/MultiVersions/multiversioninsubg'
#     for proj in result:
#         libDict = result[proj]
#         for lib in libDict:
#             d = libDict[lib]
#             for subG in d:
#                 fileName = proj+"__fdse__"+lib+"__fdse__"+str(subG)+".txt"
#                 with open(path+"\\"+fileName,'w') as f:
#                     json.dump(d[subG],f,indent=4)
#                 # li = d[subG]
#                 # for i in li:
#                     # print(i)
                    

# def printStatistics(tongji):
#     result = {}
#     result['project_number'] = len(tongji)
#     libSet = set()
#     graphSizeDict = {}
#     libSizeDict = {}
#     projlibPair = 0
#     projlibgraphCnt = 0
#     multiVersionTypeInGraphCnt = {}
#     typeMap = {}
#     for proj in tongji:
#         libDict = tongji[proj]
#         libSize = len(libDict)
#         if libSize not in libSizeDict:
#             libSizeDict[libSize] = 0
#         libSizeDict[libSize] +=1
#         projlibPair+=libSize
#         for lib in libDict:
#             libSet.add(lib)
#             d = libDict[lib]
#             projlibgraphCnt+=len(d)
#             graphSize = len(d)
#             if graphSize not in graphSizeDict:
#                 graphSizeDict[graphSize] = 0
#             graphSizeDict[graphSize] +=1 
#             for subG in d:
#                 li = d[subG]
#                 liType = harmony.checkType(li)
#                 if not liType in typeMap:
#                     typeMap[liType] = 0
#                 typeMap[liType]+=1
#                 # typeSet =  set()
#                 # for i in li:
#                 #     typeSet.add(i['flagNumber'])
#                 # typeList = list(typeSet)
#                 # typeList.sort()
#                 # key = ""
#                 # for k in typeList:
#                 #     key += str(k)+" "
#                 # if key not in multiVersionTypeInGraphCnt:
#                 #     multiVersionTypeInGraphCnt[key] = 0
#                 # multiVersionTypeInGraphCnt[key] +=1
#     result['total_lib_number'] = len(libSet)
#     result['proj_lib_pair'] = projlibPair
#     result['proj_lib_graph_cnt'] = projlibgraphCnt
#     libSizeReport = ""
#     for k in libSizeDict:
#         libSizeReport += str(k)+": "+str(libSizeDict[k])+"; "
#     result['lib_size_distribution_by_project'] = libSizeReport
#     graphSizeReport = ""
#     for k in graphSizeDict:
#         graphSizeReport +=str(k)+": "+str(graphSizeDict[k])+"; "
#     result['graph_size_distribution_by_project_lib_pair'] = graphSizeReport 
#     typeReport = ""
#     for i in typeMap:
#         typeReport += str(i)+": " +str(typeMap[i])+"; "
#     result['define_type'] = typeReport
#     # multiVersionTypeInGraphReport = ""
#     # for k in multiVersionTypeInGraphCnt:
#     #     multiVersionTypeInGraphReport +=str(k)+": "+str(multiVersionTypeInGraphCnt[k])+"; "
#     # result['multi_version_distribution_by_graph'] = multiVersionTypeInGraphReport
#     # result['multi_version_type_num'] = len(multiVersionTypeInGraphCnt)
#     with open('D:/MultiVersions/statisticalresult.json','w') as f:
#         json.dump(result,f,indent=4)



# deprecated
# def parseMV():
#     dependencyLibPath = "D:\\MultiVersions\\taggedlibversion"
#     dependencyTreePath = "D:\\MultiVersions\\tree"
#     libJsons = os.listdir(dependencyLibPath)
#     tongJiResult = {}
#     for libProj in libJsons:
#         # if libProj != '1105.txt':
#             # continue
#         # print(libProj)
#         depepdencyName = libProj[0:-4]+".json"
#         libPath = dependencyLibPath+"\\"+libProj
#         dependencyPath = dependencyTreePath+"\\"+depepdencyName
#         libNamePomFileDict = {}
#         projLibJson = openJsonFile(libPath)
#         dependencyTreeJson = openJsonFile(dependencyPath)
#         # gen tree
#         graph = genGraph(dependencyTreeJson)
#         # graph.printGraph()
#         for pomName in projLibJson:
#             libDict = projLibJson[pomName]
#             for libName in libDict:
#                 kkkey = None
#                 if not libName in libNamePomFileDict:
#                     libNamePomFileDict[libName] = []
#                 versions = libDict[libName]
#                 if len(versions)==0:
#                     continue
#                 trimVersionPathList(versions)
#                 if len(versions) > 1:
#                     can,kkkey = chooseOne(pomName,versions,graph)
#                     realVersion = can
#                 else:
#                     for key in versions:
#                         realVersion = versions[key]
#                         kkkey = key
#                 if kkkey== "null":
#                     continue
#                 libNamePomFileDict[libName].append((pomName,realVersion,kkkey))

#         # 去除 multi version
#         libNamePomFileDict =  trimMap(libNamePomFileDict)
#         libResultDict = {}
#         for libName in libNamePomFileDict:
#             result = genMultiVersionType(libName,libNamePomFileDict[libName],graph)
#             # print(result)
#             libResultDict[libName] = result
#         tongJiResult[libProj] = libResultDict
#     print(len(tongJiResult))
#     # for proj in tongJiResult:
#     #     if os.path.exists('C:/Users/huangkaifeng/Desktop/3rdParty/RQ3/api_call/'+proj):
#     #         shutil.copy('C:/Users/huangkaifeng/Desktop/3rdParty/RQ3/api_call/'+proj,"D:\\MultiVersions\\293_projs_method_calls\\"+proj)
#     #     else:
#     #         print('err:'+proj)
#     # --------------删除非多版本
#     for proj in tongJiResult:
#         liDict = tongJiResult[proj]
#         for lib in liDict:
#             subGDict = liDict[lib]
#             popKeys = []
#             for subG in subGDict:
#                 multiVersionList = subGDict[subG]
#                 rawVersionSet = set()
#                 propertyValueSet = set()
#                 for item in multiVersionList:
#                     rawVersionSet.add(item['rawVersion'])
#                     if item['propertyValue'] !=None:
#                         propertyValueSet.add(item['propertyValue'])
#                 if len(propertyValueSet)<=1 and len(rawVersionSet)<=1:
#                     popKeys.append(subG)
#             for k in popKeys:
#                 subGDict.pop(k)
    
#     popKeys = []
#     for proj in tongJiResult:
#         flag = False
#         liDict = tongJiResult[proj]
#         for lib in liDict:
#             subGDict = liDict[lib]
#             if len(subGDict)!=0:
#                 flag = True
#         if flag == False:
#             popKeys.append(proj)
#     for k in popKeys:
#         tongJiResult.pop(k)
    
#     for proj in tongJiResult:
#         liDict = tongJiResult[proj]
#         popKeys = []
#         for lib in liDict:
#             subGDict = liDict[lib]
#             if len(subGDict) == 0:
#                 popKeys.append(lib)
#         for k in popKeys:
#             liDict.pop(k)
#     # --------------删除版本解析不出来的
#     tongJiResult.pop('2735.txt')
#     # tongJiResult.pop('130.txt')
#     tongJiResult.pop('35.txt')
#     tongJiResult.pop('5027.txt')
#     # tongJiResult['5027.txt'].pop('org.hibernate__fdse__hibernate-java8__fdse__jar')
#     #------------------------
#     print('pop none:'+str(len(tongJiResult)))
#     printResult(tongJiResult)
    
#     # 删除实际version相同，不是多版本-------------------------
#     # for proj in tongJiResult:
#     #     liDict = tongJiResult[proj]
#     #     for lib in liDict:
#     #         subGDict = liDict[lib]
#     #         for subG in subGDict:
#     #             li = subGDict[subG]
#     #             ispro = set()
#     #             keys = set()
#     #             vers = set()
#     #             for i in li:
#     #                 ispro.add(i['isProperty'])
#     #                 if i['isProperty'] == True:
#     #                     keys.add(i['propertyName'])
#     #                 vers.add(i['resolved_version'])
#     #             li = list(ispro)
#     #             if len(li) == 1:
#     #                 flag = li[0]
#     #                 if flag == True and len(keys)==1 and len(vers) ==1:
#     #                     print('aadasdsadasd')

#     #----------------------------
#     # print('aaa')
#     # for proj in tongJiResult:
#     #     if os.path.exists("D:\\MultiVersions\\293_projs_api_call_with_module\\"+proj):
#     #         shutil.copy("D:\\MultiVersions\\293_projs_api_call_with_module\\"+proj,"D:\\MultiVersions\\real_multi_version_method_calls_with_module\\"+proj)
#     #     else:
#     #         print('err2:'+proj)
#     return tongJiResult

