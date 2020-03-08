package cn.edu.fudan.se.effort.m_interface.impl;

import cn.edu.fudan.se.effort.bean.VariableBean;
import cn.edu.fudan.se.effort.m_interface.IVariableCompare;
import cn.edu.fudan.se.util.SimilarityUtils;

/**
 * 变量比较实现类
 */
public class VariableCompareImpl1 implements IVariableCompare {

    @Override
    public double variableCompare(VariableBean v1, VariableBean v2) {

        double compareNameLevel = compareNameLevel(v1.getVariableName(), v2.getVariableName());
        double compareTypeLevel = compareTypeLevel(v1.getType(), v2.getType());

        return 0.5 * compareNameLevel + 0.5 * compareTypeLevel;
    }

    private double compareTypeLevel(String type1, String type2) {

        return type1.equals(type2) ? 1 : 0;
    }

    private double compareNameLevel(String variableName1, String variableName2) {
        return SimilarityUtils.lcsSimilarity(variableName1, variableName2);
    }
}
