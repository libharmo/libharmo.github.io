# coding=utf-8
import os
import json
import DepGraph
import trimPathUtil
import shutil
import harmony
import subSortOutDependency
import copy
def genStatisticString(li):
    varSet = set()
    explicitVersionDeclare = set()
    cnt = 0
    for i in li:
        if i['isProperty'] == True:
            varSet.add(i['propertyName'])
        else:
            explicitVersionDeclare.add(i['rawVersion'])
            cnt +=1
    
    return "varcnt=%d;explicitVerCnt=%d;explicitDistinctVersCnt=%d" %(len(varSet),cnt,len(explicitVersionDeclare))



def solveMV(root,tongjiResult,recVersions=None):
    statisticA = 0
    statisticB = 0
    dotPath = root+'/debug_dot'
    tainteddotPath = root+'/tainted_dot_trim'
    tainteddotErrPath = root+'/tainted_dot_trim_err'
    cnt= 0 
    for proj in tongjiResult:
        libDict = tongjiResult[proj]
        depepdencyName = proj+".json"
        dependencyTreePath = root+'/1.data overview/tree'
        dependencyPath = dependencyTreePath+"/"+depepdencyName
        dependencyTreeJson = subSortOutDependency.openJsonFile(dependencyPath)
        graph = subSortOutDependency.genGraph(dependencyTreeJson)
        # if proj == '1286' or proj == '2980' or proj == '1213':
        #     graph.toDot(dotPath,proj)
        for lib in libDict:
            subGraphDict = libDict[lib]
            dataa = lib.split('__fdse__')
            descDict = {}
            descDict['S'] = 0
            descDict['libraryname'] = lib
            descDict['N'] = 0
            descDict['Y'] = 0
            descDict['M'] = 0
            descDict['X'] = 0
            descDict['EX'] = 0
            descDict['IM'] = 0 
            XY = []
            MN = []
            if len(subGraphDict)!=1:
                statisticA+=1

            for sub in subGraphDict:
                tftf = True
                li = subGraphDict[sub]
                descDict['S'] += len(li)
                for ver in li:
                    if ver['isProperty'] == True:
                        XY.append(ver['propertyName']+"__fdse__"+ver['propertyPosition'])
                        descDict['IM'] +=1
                    else:
                        MN.append(ver['rawVersion'])
                        descDict['EX'] +=1
                
                versList = []
                for vers in li:
                    vvv = vers['usePostion']
                    versList.append(vvv)
                gNodes = []
                if proj =='816.txt':
                    continue
                for ii in versList:
                    gNodes.append(graph.getNode(ii))

                node = harmony.findCommonFatherNode(graph,gNodes)
                
                if node == None or 'remote' in node.pomsource:
                    if node == None:
                        # statisticA +=1
                        graph.toTaintedDot(proj,lib,sub,tainteddotErrPath,versList,node)
                        print(proj+" "+lib+" "+sub+'---common pom none--skip----')
                        harmony.appendNodeInfo(li,"skip","common_pom_online")
                    else:
                        if tftf:
                            statisticB+=1
                            tftf = False
                        graph.toTaintedDot(proj,lib,sub,tainteddotErrPath,versList,node)
                        # print(proj+" "+lib+" "+sub+'---pom outside--skip----')
                        harmony.appendNodeInfo(li,"skip","common_pom_online")
                    continue

                graph.toTaintedDot(proj,lib,sub,tainteddotPath,versList,node)
                #todo
                variableName = "unify."+dataa[1]+".version"    
                if recVersions == None:
                    versionValue = li[0]['resolved_version']
                    sss = genStatisticString(li)
                else:
                    recLi = recVersions[proj][lib][sub]
                    if 'recommend_version' in recLi[-1]:
                        versionValue = recLi[-1]['recommend_version']
                    else:
                        print('---------no recommended version--------')
                        harmony.appendNodeInfo(li,"skip","no_recommended_version")
                        # li[-1]['desc_dict'] = descDict
                        continue
                harmony.updateToVariable(li,"${%s}"%variableName)
                harmony.updateToUnifiedDefineNode(li,node.position,"%s=%s" % (variableName,versionValue))
                # li[-1]['desc_dict'] = descDict
            descDict['N'] += len(MN)
            descDict['Y'] += len(XY)
            descDict['M'] += len(set(MN))
            descDict['X'] += len(set(XY))
            subGraphDict['desc_dict'] = descDict
    print('staticsA:'+str(statisticA))
    print('staticsB:'+str(statisticB))
    return tongjiResult



def solve1():
    root = os.getcwd()
    with open(root +'/icfc/ic-8-18.json','r') as f:
        ic = json.load(f)
    with open(root+'/icfc/tongji_with_index-8-18.json','r') as f:
        recVersions = json.load(f)
    actions = solveMV(root,ic,recVersions)
    # with open(root+'/icfc/action-ic-8-18.json','w') as f:
    #     json.dump(actions,f,indent=4)
        
def solve2():
    root = os.getcwd()
    with open(root+'/icfc/fc-8-18.json','r') as f:
        fc = json.load(f)
    fcActions = solveMV(root,fc)
    # with open(root+'/icfc/action-fc-8-18.json','w') as f:
    #     json.dump(fcActions,f,indent=4)
