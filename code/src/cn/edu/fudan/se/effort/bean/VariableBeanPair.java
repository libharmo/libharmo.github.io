package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;
import java.util.List;

public class VariableBeanPair implements Serializable {

    private VariableBean deleteVariableBean;
    private List<VariableBean> addVariableBean;

    public VariableBeanPair() {
    }

    public VariableBean getDeleteVariableBean() {
        return deleteVariableBean;
    }

    public void setDeleteVariableBean(VariableBean deleteVariableBean) {
        this.deleteVariableBean = deleteVariableBean;
    }

    public List<VariableBean> getAddVariableBean() {
        return addVariableBean;
    }

    public void setAddVariableBean(List<VariableBean> addVariableBean) {
        this.addVariableBean = addVariableBean;
    }

    public VariableBeanPair(VariableBean deleteVariableBean, List<VariableBean> addVariableBean) {

        this.deleteVariableBean = deleteVariableBean;
        this.addVariableBean = addVariableBean;
    }
}
