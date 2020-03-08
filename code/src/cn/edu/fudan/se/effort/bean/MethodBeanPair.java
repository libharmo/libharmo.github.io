package cn.edu.fudan.se.effort.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 方法比对输出结果Pair
 */
public class MethodBeanPair implements Serializable {

    MethodBean deleteMethodBean;
    List<MethodBean> addMethodBeanList;

    public MethodBeanPair() {
    }

    public MethodBean getDeleteMethodBean() {
        return deleteMethodBean;
    }

    public void setDeleteMethodBean(MethodBean deleteMethodBean) {
        this.deleteMethodBean = deleteMethodBean;
    }

    public List<MethodBean> getAddMethodBeanList() {
        return addMethodBeanList;
    }

    public void setAddMethodBeanList(List<MethodBean> addMethodBeanList) {
        this.addMethodBeanList = addMethodBeanList;
    }

    public MethodBeanPair(MethodBean deleteMethodBean, List<MethodBean> addMethodBeanList) {

        this.deleteMethodBean = deleteMethodBean;
        this.addMethodBeanList = addMethodBeanList;
    }
}
