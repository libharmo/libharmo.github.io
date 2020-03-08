package cn.edu.fudan.se.util;

import cn.edu.fudan.se.effort.bean.ClassChangeInfo;
import cn.edu.fudan.se.effort.bean.MethodBean;
import cn.edu.fudan.se.effort.bean.VariableBean;
import cn.edu.fudan.se.effort.m_interface.IMethodCompare;
import cn.edu.fudan.se.effort.m_interface.IVariableCompare;

import java.util.*;

public class MethodCompareUtil {

    private static IMethodCompare iMethodCompare;
    private static IVariableCompare iVariableCompare;
    private static double methodThresholdLevel = 0;
    private static double variableThresholdLevel = 0;

    private static void getInstance(IMethodCompare iMethodCompare) {
        setIMethodCompare(iMethodCompare);
    }

    private static void getInstance(IVariableCompare iVariableCompare) {
        setIVariableCompare(iVariableCompare);
    }

    public static void getInstance(IMethodCompare iMethodCompare, IVariableCompare iVariableCompare) {
        getInstance(iMethodCompare);
        getInstance(iVariableCompare);
    }


    public static void setIVariableCompare(IVariableCompare iVariableCompare) {
        MethodCompareUtil.iVariableCompare = iVariableCompare;
    }

    private static void setIMethodCompare(IMethodCompare iMethodCompare) {
        MethodCompareUtil.iMethodCompare = iMethodCompare;
    }

    private static double compareMethod(MethodBean m1, MethodBean m2) {

        double level = iMethodCompare.methodCompare(m1, m2);
        return level;
    }

    private static double compareVariable(VariableBean v1, VariableBean v2) {

        double level = iVariableCompare.variableCompare(v1, v2);
        return level;
    }

    /**
     * 比较方法
     *
     * @param m1
     * @param map
     * @return
     */
    public static List<MethodBean> compareMethods(MethodBean m1, Map<String, ClassChangeInfo> map) {

        Queue<MethodBean> priorityQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1.getLevel() < o2.getLevel()) {
                return 1;
            } else if (o1.getLevel() > o2.getLevel()) {
                return -1;
            }
            return 0;
        });

        for (Map.Entry<String, ClassChangeInfo> entry : map.entrySet()) {

            ClassChangeInfo classChangeInfo = entry.getValue();

            List<MethodBean> methodBeanList = classChangeInfo.getMethodBeanList();

            for (MethodBean m2 : methodBeanList) {
                double level = compareMethod(m1, m2);
                m2.setLevel(level);
                priorityQueue.add(m2);
            }
        }

        List<MethodBean> topMethodBeanList = getTopQueueItem(priorityQueue, 10);


        return topMethodBeanList;
    }

    /**
     * 获得level最高的10个item
     *
     * @param priorityQueue
     * @return
     */
    private static List getTopQueueItem(Queue priorityQueue, int count) {
        List methodBeanList = new ArrayList<>();

        while (!priorityQueue.isEmpty() && count > 0) {

            methodBeanList.add(priorityQueue.poll());
            count--;
        }


        return methodBeanList;
    }

    public static List<VariableBean> compareVariable(VariableBean v1, Map<String, ClassChangeInfo> map) {

        Queue<VariableBean> priorityQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1.getLevel() < o2.getLevel()) {
                return 1;
            } else if (o1.getLevel() > o2.getLevel()) {
                return -1;
            }
            return 0;
        });

        for (Map.Entry<String, ClassChangeInfo> entry : map.entrySet()) {
            ClassChangeInfo classChangeInfo = entry.getValue();
            List<VariableBean> variableBeanList = classChangeInfo.getVariableBeanList();
            for (VariableBean v2 : variableBeanList) {
                double level = compareVariable(v1, v2);
                v2.setLevel(level);
                priorityQueue.add(v2);

            }
        }

        List<VariableBean> variableBeanList = getTopQueueItem(priorityQueue, 10);

        return variableBeanList;
    }

}
