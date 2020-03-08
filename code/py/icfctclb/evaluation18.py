import os
import json


with open('icfc/ic-8-18.json','r') as f:
    ic = json.load(f)
with open('icfc/fc-8-18.json','r') as f:
    fc = json.load(f)
# with open('modules_maven-id.json','r') as f:
#     modulesize = json.load(f)
with open('pom_count443-id.json','r') as f:
    modulesize = json.load(f)
with open('icfc/tc-8-18.json','r') as f:
    tc = json.load(f)
with open('icfc/sl.json','r') as f:
    sl = json.load(f)

def toExcel(fcoricortc,name,flag,title,ff):
    ddd = {}
    for proj in fcoricortc:
        for lib in fcoricortc[proj]:
            if not lib in ddd:
                ddd[lib] = 0
            ddd[lib]+=1
    aaa = sorted(ddd.items(), key=lambda d: d[1],reverse = True)
    # sorted(d.items(), lambda x, y: cmp(x[1], y[1]), reverse=True) 
    cnt  = 0
    print(title)
    ff.write(title)
    ff.write("\n")
    for e in aaa:
        print(e)
        libName = e[0]
        count = e[1]
        libName = libName.split('__fdse__')
        ff.write(libName[0]+" "+libName[1])
        ff.write(",")
        ff.write(str(e[1]))
        ff.write("\n")
        if cnt>20:
            break
        cnt+=1
        # print(aaa[e])
    
def plana():
    ff = open('Top20.csv','w')
    toExcel(ic,'ic',True,'IC by projects',ff)
    toExcel(fc,'fc',True,'FC by projects',ff)
    toExcel(tc,'tc',True,'TC by projects',ff)
    ff.close()

    # toExcel(ic,'ic',False,'IC by cases')
    # toExcel(fc,'fc',False,'FC by cases')
    # toExcel(tc,'tc',False,'TC by cases')

def planb():
    icfc = {}
    tcd = {}
    for proj in ic:
        for lib in ic[proj]:
            if not lib in icfc:
                icfc[lib] = 0
            icfc[lib]+=1
    for proj in fc:
        for lib in fc[proj]:
            if not lib in icfc:
                icfc[lib] = 0
            icfc[lib]+=1
    for proj in tc:
        for lib in tc[proj]:
            if not lib in tcd:
                tcd[lib] = 0
            tcd[lib] +=1
    ff = open('Top20-icfc-tc.csv','w')
    aaa = sorted(icfc.items(), key=lambda d: d[1],reverse = True)
    bbb = sorted(tcd.items(), key=lambda d: d[1],reverse = True)
    cnt  = 0
    ff.write("IC+FC")
    ff.write("\n")
    for e in aaa:
        print(e)
        libName = e[0]
        count = e[1]
        libName = libName.split('__fdse__')
        ff.write(libName[0]+" "+libName[1])
        ff.write(",")
        ff.write(str(e[1]))
        ff.write("\n")
        if cnt>20:
            break
        cnt+=1
    
    ff.write("TC")
    ff.write("\n")
    cnt  = 0
    for e in bbb:
        print(e)
        libName = e[0]
        count = e[1]
        libName = libName.split('__fdse__')
        ff.write(libName[0]+" "+libName[1])
        ff.write(",")
        ff.write(str(e[1]))
        ff.write("\n")
        if cnt>20:
            break
        cnt+=1

planb()