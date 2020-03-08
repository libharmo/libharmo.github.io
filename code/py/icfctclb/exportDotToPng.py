import os
import subprocess
from subprocess import Popen,PIPE
path = 'D:/MultiVersions/tainted_dot_trim'
output = 'D:/MultiVersions/pngs'
files = os.listdir(path)
for file in files:
    cmd = '\"C:/Program Files (x86)/Graphviz2.38/bin/dot.exe\"' +' -Tpng -o '+"\""+output+'/'+file[0:-4]+".png\" "+ "\""+path+'/'+file+"\""
    print(cmd)
    
