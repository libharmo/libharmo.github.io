package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 类变化的信息
 */
public class ClassChangeInfo implements Serializable {

    List<MethodBean> methodBeanList;
    List<VariableBean> variableBeanList;

    public ClassChangeInfo() {
    }

    public List<MethodBean> getMethodBeanList() {

        return methodBeanList;
    }

    public void setMethodBeanList(List<MethodBean> methodBeanList) {
        this.methodBeanList = methodBeanList;
    }

    public List<VariableBean> getVariableBeanList() {
        return variableBeanList;
    }

    public void setVariableBeanList(List<VariableBean> variableBeanList) {
        this.variableBeanList = variableBeanList;
    }

    public ClassChangeInfo(List<MethodBean> methodBeanList, List<VariableBean> variableBeanList) {

        this.methodBeanList = methodBeanList;
        this.variableBeanList = variableBeanList;
    }
}
