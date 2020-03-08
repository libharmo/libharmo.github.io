package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;

/**
 * 变量Bean
 */
public class VariableBean implements Serializable, Cloneable {

    String type;
    String variableName;
    String value;
    String fileName;
    double level;

    public void setLevel(double level) {
        this.level = level;
    }

    public double getLevel() {
        return level;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public VariableBean(String type, String variableName, String value, String fileName) {

        this.type = type;
        this.variableName = variableName;
        this.value = value;
        this.fileName = fileName;
    }

    public VariableBean() {
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
