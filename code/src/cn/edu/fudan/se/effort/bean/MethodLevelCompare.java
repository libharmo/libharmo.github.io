package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;
import java.util.Map;

public class MethodLevelCompare implements Serializable {

    Map<String, ClassChangeInfo> addMethodMap;
    Map<String, ClassChangeInfo> deleteMethodMap;

    public Map<String, ClassChangeInfo> getAddMethodMap() {
        return addMethodMap;
    }

    public void setAddMethodMap(Map<String, ClassChangeInfo> addMethodMap) {
        this.addMethodMap = addMethodMap;
    }

    public Map<String, ClassChangeInfo> getDeleteMethodMap() {
        return deleteMethodMap;
    }

    public void setDeleteMethodMap(Map<String, ClassChangeInfo> deleteMethodMap) {
        this.deleteMethodMap = deleteMethodMap;
    }

    public MethodLevelCompare() {

    }

    public MethodLevelCompare(Map<String, ClassChangeInfo> addMethodMap, Map<String, ClassChangeInfo> deleteMethodMap) {

        this.addMethodMap = addMethodMap;
        this.deleteMethodMap = deleteMethodMap;
    }
}
