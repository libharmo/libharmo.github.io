package cn.edu.fudan.se.effort.m_interface.impl;

import cn.edu.fudan.se.effort.bean.MethodBean;
import cn.edu.fudan.se.effort.m_interface.IMethodCompare;
import cn.edu.fudan.se.util.SimilarityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法比较实现类
 */
public class MethodCompareImpl1 implements IMethodCompare {
    @Override
    public double methodCompare(MethodBean m1, MethodBean m2) {
        double compareMethodName = compareMethodName(m1.getMethodName(), m2.getMethodName());
        double compareReturnType = compareReturnType(m1.getReturnType(), m2.getReturnType());
        double compareParams = compareMethodParams(m1.getParams(), new ArrayList<>(m2.getParams()));

        return 0.5 * compareMethodName + 0.25 * compareReturnType + 0.25 * compareParams;

    }

    /**
     * 比较参数名
     *
     * @param params1
     * @param params2
     * @return
     */
    private double compareMethodParams(List<String> params1, List<String> params2) {

/*        String param1String = generateString(params1);
        String param2String = generateString(params2);

        return SimilarityUtils.compareCosineSimilarity(param1String, param2String);*/

        return SimilarityUtils.tokenSimilarity(params1, params2);
    }

    private String generateString(List<String> params1) {
        String result = "";

        for (String param1 : params1) {
            result += param1 + " ";
        }

        return result.trim();
    }

    private double compareReturnType(String m1ReturnType1, String m2ReturnType2) {

        return m1ReturnType1.equals(m2ReturnType2) ? 1 : 0;

    }

    /**
     * 比较方法相似度
     *
     * @param methodName1
     * @param methodName2
     * @return
     */
    private double compareMethodName(String methodName1, String methodName2) {

     /*   double similarity = SimilarityUtils.lcsSimilarity(methodName1, methodName2);

        return similarity;*/

        return SimilarityUtils.tokenSimilarity(methodName1, methodName2);
    }

}
