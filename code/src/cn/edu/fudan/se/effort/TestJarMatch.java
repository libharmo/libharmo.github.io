package cn.edu.fudan.se.effort;

import cn.edu.fudan.se.effort.bean.*;
import cn.edu.fudan.se.effort.m_interface.IMethodCompare;
import cn.edu.fudan.se.effort.m_interface.IVariableCompare;
import cn.edu.fudan.se.effort.m_interface.impl.MethodCompareImpl1;
import cn.edu.fudan.se.effort.m_interface.impl.VariableCompareImpl1;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.MethodCompareUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJarMatch {

    static MethodLevelCompareResult methodLevelCompareResult;


    public static void main(String[] args) throws CloneNotSupportedException {
        Map<String, MethodBeanPair> methodBeanPairMap = new HashMap<>();
        Map<String, VariableBeanPair> variableBeanPairMap = new HashMap<>();

        String content = FileUtil.read("H:\\shibowen\\method_level_compare/" + "result.txt");
        MethodLevelCompare methodLevelCompare = new Gson().fromJson(content, new TypeToken<MethodLevelCompare>() {
        }.getType());
        Map<String, ClassChangeInfo> addMethodMap = methodLevelCompare.getAddMethodMap();
        Map<String, ClassChangeInfo> deleteMethodMap = methodLevelCompare.getDeleteMethodMap();

        IMethodCompare iMethodCompare = new MethodCompareImpl1();
        IVariableCompare iVariableCompare = new VariableCompareImpl1();

        MethodCompareUtil.getInstance(iMethodCompare, iVariableCompare);

        MethodBeanPair methodBeanPair = null;
        VariableBeanPair variableBeanPair = null;

        int fileSize = deleteMethodMap.size();

        int fileIndex = 0;

        for (Map.Entry<String, ClassChangeInfo> deleteMethodMapEntry : deleteMethodMap.entrySet()) {
            System.out.println(String.valueOf(fileIndex) + "/" + String.valueOf(fileSize));
            fileIndex++;
            String file = deleteMethodMapEntry.getKey();
            ClassChangeInfo classChangeInfo = deleteMethodMapEntry.getValue();

            List<MethodBean> methodBeanList = classChangeInfo.getMethodBeanList();
            List<VariableBean> variableBeanList = classChangeInfo.getVariableBeanList();

            for (MethodBean m1 : methodBeanList) {
                List<MethodBean> tempList = MethodCompareUtil.compareMethods(m1, addMethodMap);
                List<MethodBean> m2List = generateMethodBeanList(tempList);
                methodBeanPair = new MethodBeanPair(m1, m2List);
                methodBeanPairMap.put(file + "__fdse__" + m1.getMethodName() + "__fdse__" + m1.paramsToString(), methodBeanPair);
            }

            for (VariableBean v1 : variableBeanList) {
                List<VariableBean> tempList = MethodCompareUtil.compareVariable(v1, addMethodMap);
                List<VariableBean> v2List = generateVariableBeanList(tempList);
                variableBeanPair = new VariableBeanPair(v1, v2List);
                variableBeanPairMap.put(file + "__fdse__" + v1.getVariableName(), variableBeanPair);
            }

            methodLevelCompareResult = new MethodLevelCompareResult(methodBeanPairMap, variableBeanPairMap);
            methodLevelCompareResult.count();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtil.writeFlie("H:\\shibowen\\method_level_compare_result/" + "result.txt", gson.toJson(methodLevelCompareResult));


    }

    private static List<VariableBean> generateVariableBeanList(List<VariableBean> tempList) throws CloneNotSupportedException {

        List<VariableBean> variableBeanList = new ArrayList<>();

        VariableBean tempBean;

        for (VariableBean variableBean : tempList) {
            tempBean = (VariableBean) variableBean.clone();
            variableBeanList.add(tempBean);
        }

        return variableBeanList;
    }

    private static List<MethodBean> generateMethodBeanList(List<MethodBean> tempList) throws CloneNotSupportedException {
        List<MethodBean> methodBeanList = new ArrayList<>();

        MethodBean tempBean;

        for (MethodBean methodBean : tempList) {
            tempBean = (MethodBean) methodBean.clone();
            methodBeanList.add(tempBean);
        }

        return methodBeanList;
    }

}
