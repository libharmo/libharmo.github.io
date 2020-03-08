package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 方法bean
 */
public class MethodBean implements Serializable, Cloneable {

    String methodName;
    String returnType;
    List<String> params;
    String body;

    String fileName;

    public String paramsToString() {
        if (params == null || params.size() == 0) {
            return "";
        }

        String result = "";
        for (String param : params) {
            result += param + "__fdse__";
        }

        return result;

    }

    double level;

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public MethodBean() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MethodBean(String methodName, String returnType, List<String> params, String body, String fileName) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.params = params;
        this.body = body;
        this.fileName = fileName;
    }

    public String getMethodName() {

        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
