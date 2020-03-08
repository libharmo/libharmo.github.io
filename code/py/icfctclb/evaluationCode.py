import os
import json
import subEvaluationCode
# ic.proj: 152 ic.projlib: 621
# fc.proj: 346 fc.projlib: 2576
# tc.proj: 318 tc.projlib: 4413
# Proj:433, Proj-Lib:11906
# Merge ic and fc len:364
# FC Total:2576
# FC Mixed:98
# FC EX:2423
# FC IM:55
# IC Total:621
# IC Mixed:131
# IC EX:224
# IC IM:266

#  21 pm
# ic.proj: 151 ic.projlib: 609
# fc.proj: 175 fc.projlib: 1096
# tc.proj: 389 tc.projlib: 5758
# sl.proj: 433 sl.projlib: 12106
with open('icfc/ic-8-21-pm.json','r') as f:
    ic = json.load(f)
with open('icfc/fc-8-21-pm.json','r') as f:
    fc = json.load(f)
# with open('modules_maven-id.json','r') as f:
#     modulesize = json.load(f)
with open('pom_count443-id.json','r') as f:
    modulesize = json.load(f)
with open('icfc/tc-8-21-pm.json','r') as f:
    tc = json.load(f)
with open('icfc/sl-8-21-pm.json','r') as f:
    sl = json.load(f)

subEvaluationCode.fig5a_Pie1(ic,fc,tc,sl)
print('--------------------------')
subEvaluationCode.fig5b_Pie2(ic,fc,tc,sl)
print('--------------------------')
subEvaluationCode.figure9ab(ic,fc)
print('--------------------------')
subEvaluationCode.fig8a(ic,fc,tc)
print('--------------------------')
subEvaluationCode.fig8bDistinctVersion(ic)
print('--------------------------')
subEvaluationCode.fig7a7b_icfcpercentage(ic,fc,modulesize)
print('--------------------------')
subEvaluationCode.overviewModuleDataFor4types(ic,fc,tc,sl,modulesize)








