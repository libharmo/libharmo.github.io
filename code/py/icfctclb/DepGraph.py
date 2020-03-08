import os
import queue
import copy
class Node(object):
    def __init__(self,idd,pos,pom_source):
        self.position = pos
        self.defineType = False
        self.useType = False
        self.pomsource = pom_source
        # lib
        self.libFlagNum = 0 
        self.id = idd

    def setDefUse(self,a,b):
        self.defineType = a
        self.useType = b
    
    def clearFlagNum(self):
        self.clearLibFlagNum = 0


class DepGraph(object):
    def __init__(self):
        self.node = []
        self.edge = {}
        self.biedge = {}
        self.cluster = None
        self.labelDict = {}

    def addNode(self,position,pomsource):
        node = Node(len(self.node),position,pomsource)
        self.node.append(node)
        self.labelDict[position] = node


    def getNode(self,position):
        if position.startswith('E:/data/multiversion/pom/'):
            position = position.replace('E:/data/multiversion/pom/','')
        if position.startswith('C:/data/pom/'):
            position = position.replace('C:/data/pom/','')
        if position not in self.labelDict:
            print('key error:'+position)
            return None
        return self.labelDict[position]

    def addEdge(self,nodeAPosition,nodeBPosition):
        nodeA = self.labelDict[nodeAPosition]
        nodeB = self.labelDict[nodeBPosition]
        if not nodeA in self.edge:
            self.edge[nodeA] = []
        li = self.edge[nodeA]
        if not nodeB in li:
            self.edge[nodeA].append(nodeB)

        if not nodeA in self.biedge:
            self.biedge[nodeA] = []
        lia = self.biedge[nodeA]
        if not nodeB in lia:
            self.biedge[nodeA].append(nodeB)
        if not nodeB in self.biedge:
            self.biedge[nodeB] = []
        lib = self.biedge[nodeB]
        if not nodeA in lib:
            self.biedge[nodeB].append(nodeA)

    def clustering(self):
        m_cluster = []
        flag = {}
        for head in self.node:
            # start
            subGraph = []
            q = queue.Queue()
            q.put(head)
            while q.qsize()>0:
                qnode = q.get()
                if qnode in flag:
                    continue
                subGraph.append(qnode)
                flag[qnode] = 1
                if qnode in self.biedge:
                    neighbers = self.biedge[qnode]
                    for nodeNeighber in neighbers:
                        if not nodeNeighber in flag: 
                            q.put(nodeNeighber)
            if len(subGraph) ==0:
                continue
            m_cluster.append(subGraph)
        # print(m_cluster)
        self.cluster = m_cluster
        return m_cluster
    
    def printGraph(self):
        for subG in self.cluster:
            print('------------------------------')
            for node in subG:
                print(node.position)

    def isNodeExist(self,nodePosition):
        if nodePosition in self.labelDict:
            return True
        return False

    def removePrefix(self,position):
        if position.startswith('D:/Workspace/WyRepos/pom/'):
            position = position.replace('D:/Workspace/WyRepos/pom/','')
        return position
    # 有向 a->b的step
    def step(self,nodeALabel,nodeBLabel):
        nodeA = self.labelDict[nodeALabel]
        nodeB = self.labelDict[nodeBLabel]
        if self.cluster == None:
            self.clustering()
        flag = False
        for subG in self.cluster:
            if nodeA in subG and nodeB in subG:
                flag = True
        if not flag:
            return -1
        stepCnt = self.visit(nodeA,nodeB,0,{})
        if stepCnt!=-1:
            return stepCnt
        else:
            stepCnt = self.visit(nodeB,nodeA,0,{})
            return stepCnt

    def visit(self,nodeX,nodeY,stepCnt,flag):
        if nodeX == nodeY:
            return stepCnt
        if not nodeX in self.edge:
            return -1
        parent = self.edge[nodeX]
        flag[nodeX] = 1
        minCnt = 100000000
        for par in parent:
            if par in flag:
                continue
            stepCntPar = self.visit(par,nodeY,stepCnt+1,flag)
            if stepCntPar < minCnt and stepCntPar>0:
                minCnt = stepCntPar
        if minCnt == 100000000:
            return -1
        return minCnt

    def stepBi(self,nodeALabel,nodeBLabel):
        nodeA = self.labelDict[nodeALabel]
        nodeB = self.labelDict[nodeBLabel]
        if self.cluster == None:
            self.clustering()
        flag = False
        for subG in self.cluster:
            if nodeA in subG and nodeB in subG:
                flag = True
        if not flag:
            return -1
        stepCnt = self.visitBi(nodeA,nodeB,0,{})
        if stepCnt!=-1:
            return stepCnt
        else:
            stepCnt = self.visitBi(nodeB,nodeA,0,{})
            return stepCnt

    def visitBi(self,nodeX,nodeY,stepCnt,flag):
        if nodeX == nodeY:
            return stepCnt
        if not nodeX in self.edge:
            return -1
        parent = self.biedge[nodeX]
        flag[nodeX] = 1
        minCnt = 100000000
        for par in parent:
            if par in flag:
                continue
            stepCntPar = self.visit(par,nodeY,stepCnt+1,flag)
            if stepCntPar < minCnt and stepCntPar>0:
                minCnt = stepCntPar
        if minCnt == 100000000:
            return -1
        return minCnt

    def addLibLabelToNode(self,loc,flagNum):
        self.labelDict[loc].libFlagNum = flagNum

    def clearLibFlagNum(self):
        for n in self.node:
            n.clearLibFlagNum()

    def getIndexOfSubGraphByNodeLabel(self,position):
        nod = self.getNode(position)
        for i in range(0,len(self.cluster)):
            subG = self.cluster[i]
            for tempNo in subG:
                if tempNo == nod:
                    return i
        print('eeeee==================')
        return -1

    def findFatherNode(self,node,m_li):
        m_li.append(node)
        if not node in self.edge:
            return [m_li]
        fa = self.edge[node]
        res = []
        flag = False
        for i in fa:
            if flag:
                m_li = copy.copy(m_li)
            m_list = self.findFatherNode(i,m_li)
            for li in m_list:
                res.append(li)
        return res

    def findOneCommonAncestor(self,node1,node2):
        fathers = self.findFatherNode(node1,[])
        q = queue.Queue()
        q.put(node2)
        while q.qsize()!=0:
            n = q.get()
            for fatherli in fathers:
                if n in fatherli:
                    return n
            if not n in self.edge:
                continue
            fa = self.edge[n]
            for f in fa:
                q.put(f)
        return None


    def toDot(self,path,name):
        cnt = 0
        for c in self.cluster:
            newfile = path+'/'+ name +"_"+ str(cnt)+".dot"
            cnt +=1
            with open(newfile,'w') as f:
                f.write('digraph "Title" {\n\tlabel="title";\n\tnode [shape=box];\n')
                for node in c:
                    if node.pomsource !='local':
                        line = '\t"%d" [style=filled,fillcolor=gray,label="%s",]; \n' %(node.id,node.position)
                    else:
                        line = '\t"%d" [label="%s",]; \n' %(node.id,node.position)
                    f.write(line)
                
                    if node in self.edge:
                        li = self.edge[node]
                        for dstNode in li:
                            line = '\t"%d"->"%d";\n' % (node.id,dstNode.id)
                            f.write(line)
                f.write('}')
            
    def toTaintedDot(self,projName,libName,subGraphId,outPath,tainted_paths,commonNode):
        taintedNodes = []
        for path in tainted_paths:
            nod = self.getNode(path)
            taintedNodes.append(nod)
        c = self.cluster[int(subGraphId)]
        libName = libName.replace('__fdse__','____')
        newfile = outPath+'/'+ projName +"_"+libName+"_"+ subGraphId+".dot"
        allNodes = set()
        for t in taintedNodes:
            allNodes.add(t)
            q = queue.Queue()
            q.put(t)
            while q.qsize()>0:
                qnode = q.get()
                if qnode in self.edge:
                    li = self.edge[qnode]
                    for dstNode in li:
                        allNodes.add(dstNode)
                        q.put(dstNode)
        
    
        with open(newfile,'w') as f:
            f.write('digraph "" {\n\tlabel="";\n\tnode [shape=box];\n')
            # f.write('digraph "" {\n\tnode [shape=box];\n    subgraph cluster_01{ \nlabel = "Legend";\nrankdir = TB;\n"e1" [label="Inconsistent lib pom file",fontcolor=red];\n "e2" [label="Local pom file",fontcolor=black]; \n"e3" [label="Remote pom file",style=filled,fillcolor=gray];\n"e4" [label="Recommendded lib pom file",fontcolor=green,]; \n"e1"->"e2" [color=white];\n"e2"->"e3" [color=white];\n"e3"->"e4" [color=white];}')
            # for node in c:
            # f.write('subgraph cluster_02 { \nlabel="     ";\n')
            for node in allNodes:
                color = ''
                if node in taintedNodes:
                    color = 'fontcolor=red,color=forestgreen,penwidth=2.0,'

                if commonNode!=None and node == commonNode:
                    color += 'color=forestgreen,penwidth=2.0,'
                    
                if node.pomsource !='local': 
                    line = '\t"%d" [style=filled,fillcolor=gray,label="%s",%s]; \n' %(node.id,node.position,color)
                else:
                    line = '\t"%d" [label="%s",%s]; \n' %(node.id,node.position,color)
                f.write(line)
            
                if node in self.edge:
                    li = self.edge[node]
                    for dstNode in li:
                        if dstNode in allNodes:
                            line = '\t"%d"->"%d";\n' % (node.id,dstNode.id)
                            f.write(line)
            # f.write('}\n}')
            f.write('}')

        
def test():
    g = DepGraph()
    # g.addNode(1)
    # g.addNode(2)
    # g.addNode(3)
    # g.addNode(4)
    # g.addNode(5)
    # g.addEdge(1,2)
    # g.addEdge(2,3)
    # g.addEdge(4,5)
    # g.clustering()
    s = g.step(1,3)
    a = g.step(1,4)
    print(s)
    print(a)

# test()

    

