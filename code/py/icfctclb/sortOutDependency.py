# coding=utf-8
import os
import json
import DepGraph
import trimPathUtil
import shutil
import harmony
import subSortOutDependency
import copy
import sortOutDependencyStep1
import sortOutDependencySolve


def copyAPICallFile(tongJiResult):
    # api call and api call with module
    for proj in tongJiResult:
        if os.path.exists('C:/Users/calvi/Desktop/3rdParty/RQ3/api_call/'+proj):
            shutil.copy('C:/Users/calvi/Desktop/3rdParty/RQ3/api_call/'+proj,"D:\\MultiVersions\\293_projs_method_calls\\"+proj)
        else:
            print('err:'+proj)

    for proj in tongJiResult:
        if os.path.exists("D:\\MultiVersions\\293_projs_api_call_with_module\\"+proj):
            shutil.copy("D:\\MultiVersions\\293_projs_api_call_with_module\\"+proj,"D:\\MultiVersions\\real_multi_version_method_calls_with_module\\"+proj)
        else:
            print('err2:'+proj)

def numberOfModuleProjects(meta):
    for proj in meta:
        data = meta[proj]
        if proj == '1022':
            k = None
            for key in data:
                if 'target' in key:
                    k = key
            data.pop(k)
def main():
    # # unsed generate()
    # original
    phase = 1
    # reset
    sortOutDependencyStep1.step1(phase)
    # 20 pm big fix
    # sortOutDependencySolve.solve1()
    # sortOutDependencySolve.solve2()
    
main()












