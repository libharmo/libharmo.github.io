package cn.edu.fudan.se.effort.bean;

import java.util.Map;

public class MethodLevelCompareResult {

    private Map<String, MethodBeanPair> methodBeanPairMap;
    private Map<String, VariableBeanPair> variableBeanPairMap;

    int deleteMethodSize;
    int deleteVariableSize;

    public void count() {
        deleteMethodSize = methodBeanPairMap.size();
        deleteVariableSize = variableBeanPairMap.size();


    }

    public MethodLevelCompareResult() {
    }

    public MethodLevelCompareResult(Map<String, MethodBeanPair> methodBeanPairMap, Map<String, VariableBeanPair> variableBeanPairMap) {

        this.methodBeanPairMap = methodBeanPairMap;
        this.variableBeanPairMap = variableBeanPairMap;
    }

    public Map<String, MethodBeanPair> getMethodBeanPairMap() {

        return methodBeanPairMap;
    }

    public void setMethodBeanPairMap(Map<String, MethodBeanPair> methodBeanPairMap) {
        this.methodBeanPairMap = methodBeanPairMap;
    }

    public Map<String, VariableBeanPair> getVariableBeanPairMap() {
        return variableBeanPairMap;
    }

    public void setVariableBeanPairMap(Map<String, VariableBeanPair> variableBeanPairMap) {
        this.variableBeanPairMap = variableBeanPairMap;
    }
}
