import os
import json
import queue
#----------Step 1 version 统一
# {1: 305, 3: 209, 2: 312}
# case 1 没有property 305
# case 2 只有property 312
# case 3 两个都有 209
# case 2 定义的变量数目分布
# { 1: 148, 2: 138, 3: 21, 4: 4, 5: 1}
# case 3 混合的情况 定义的变量分布
# {1: 190, 2: 16,   3: 2, 4: 1}

# Effort: 原子操作记录需要统一需要花费的代价
# Atom operation: 1.alter version
#                 2.alter variable value
#                 3.alter variable name
#                 4.move varialbe define loc

# Risk: 统一可能存在的risk
# Atom risk level:
#  1. safe: 没有可见的risk API/API call graph路径都没有risk
#  2. minor: 版本存在issue
#  3. medium: API call graph路径method change
#  3. risky: risk API/API call graph 路径存在risky method
#  4. crashable: API deletion

# 可选择策略 effort优先还是risk优先

def checkType(m):
    ispro = set()
    keys = set()
    for i in m:
        ispro.add(i['isProperty'])
    li = list(ispro)
    if len(li) == 1:
        flag = li[0]
        if flag == False:
            # 统一version Explicit
            return 1
        else:
            # 统一version Inplicit
            return 2
    else:
        # hybrid
        return 3

# 显式版本统一
def unifyExplicitVersion(m,version):
    step = 0 
    for i in m:
        if i['isProperty'] == False:
            i['rawVersion'] = version
            step+=1
    return step

# 隐式版本统一
def unifyInplicitVersion(m,version,variable_name):
    step = 0 
    for i in m:
        if i['isProperty'] == True:
            i['rawVersion'] = '$'+variable_name
            i['propertyName'] = variable_name
            step+=1

# location 统一
def recommend3(m,version,position):
    step = 0 
    for i in m:
        if i['isProperty'] == True:
            i['propertyPosition'] = position
            step+=1

def selectPropertyName(m):
    m = {}
    for i in m:
        if i['isProperty'] == True:
            name = i['propertyName']
            if not name in m:
                m[name] = 0
            m[name] +=1
      
    


def findCommonFatherNode(graph,childNodes):
    # q = queue.Queue()
    # for n in childNodes:
    #     q.put(n)
    # while q.qsize()!=0:
    #     n = q.get()
    node1 = childNodes.pop()
    for node2 in childNodes:
        node1 = graph.findOneCommonAncestor(node1,node2)
    return node1




def updateToVariable(li,s):
    for i in li:
        i['action_update_version_symbol'] = s
    

def updateToUnifiedDefineNode(li,pos,s):
    newElement = {}
    newElement['action_update_define_pos'] = pos
    newElement['action_update_define_version_value'] = s
    li.append(newElement)
    
def appendNodeInfo(li,key,value):
    newElement = {}
    newElement[key] = value
    li.append(newElement)





