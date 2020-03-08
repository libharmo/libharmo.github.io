import os

def trimPathSub(path):
    path = path.replace("\\","/")
    data = path.split("/")
    a = set()
    for i in range(0,len(data)):
        temp = data[i]
        if i!=0 and ".." == temp and data[i-1]!="..":
            a.add(i-1)
            a.add(i)
        if i!=0 and "." == temp:
            a.add(i)
    res = ""
    for i in range(0,len(data)):
        if data[i] =='':
            continue
        if i in a:
            continue
        else:
            res+=data[i]
            res+="/"
    if path.endswith("/"):
        return res
    return res[0:-1]

def trimPath(path):
    path2 = path
    newPath = None
    while True:
        newPath = trimPathSub(path2)
        if newPath != path2:
            path2 = newPath
        else:
            break
    return newPath