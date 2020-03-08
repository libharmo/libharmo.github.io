package cn.edu.fudan.se.util;

import cn.edu.fudan.se.effort.bean.ClassChangeInfo;
import cn.edu.fudan.se.effort.bean.MethodBean;
import cn.edu.fudan.se.effort.bean.VariableBean;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class BodyDeclarationUtil {


    /**
     * 比较两个bodyDeclaration
     *
     * @param b1
     * @param b2
     * @return
     */
    public static boolean compareBodyDeclaration(BodyDeclaration b1, BodyDeclaration b2) {
        /**
         * 比较
         * 1. b1 b2 的类型
         * 2. 返回类型
         * 3. 方法名
         * 4. 参数列表
         */
        String b1Class = b1.getClass().toString();
        String b2Class = b2.getClass().toString();
        if (!b1Class.equals(b2Class)) {
            return false;
        }

        if (b1 instanceof FieldDeclaration) {
            return compareFieldDeclaration(b1, b2);
        }
        if (b1 instanceof MethodDeclaration) {
            return compareMethodDeclaration(b1, b2);
        }


        return false;
    }

    /**
     * 比较两个MethodDeclaration
     *
     * @param b1
     * @param b2
     * @return
     */
    private static boolean compareMethodDeclaration(BodyDeclaration b1, BodyDeclaration b2) {
        String b1MethodDeclarationName = ((MethodDeclaration) b1).getName().getIdentifier();
        List b1MethodDeclarationParams = ((MethodDeclaration) b1).parameters();
        String b1ReturnType = ((MethodDeclaration) b1).getReturnType2() == null ? "" : ((MethodDeclaration) b1).getReturnType2().toString();

        String b2MethodDeclarationName = ((MethodDeclaration) b2).getName().getIdentifier();
        List b2MethodDeclarationParams = ((MethodDeclaration) b2).parameters();
        String b2ReturnType = ((MethodDeclaration) b2).getReturnType2() == null ? "" : ((MethodDeclaration) b2).getReturnType2().toString();

        return b1MethodDeclarationName.equals(b2MethodDeclarationName) && b1ReturnType.equals(b2ReturnType) && compareParams(b1MethodDeclarationParams, b2MethodDeclarationParams);


    }

    /**
     * 比较参数列表
     *
     * @param b1Params
     * @param b2Params
     * @return
     */
    private static boolean compareParams(List b1Params, List b2Params) {

        //都为空
        if (b1Params == null && b2Params == null) return true;

        //只有一个空
        if (b1Params == null || b2Params == null) return false;

        //都不空
        if (b1Params.size() != b2Params.size()) {
            return false;
        }

        //参数列表长度相同
        for (int i = 0; i < b1Params.size(); i++) {
            String b1Param = ((SingleVariableDeclaration) b1Params.get(i)).getType().toString();
            String b2Param = ((SingleVariableDeclaration) b2Params.get(i)).getType().toString();

            if (!b1Param.equals(b2Param)) {
                return false;
            }

        }
        return true;
    }

    /**
     * 比较两个FieldDeclaration
     *
     * @param b1
     * @param b2
     * @return
     */
    private static boolean compareFieldDeclaration(BodyDeclaration b1, BodyDeclaration b2) {

        String b1Type = ((FieldDeclaration) b1).getType().toString();
        String b1VariableName = ((VariableDeclarationFragment) ((FieldDeclaration) b1).fragments().get(0)).getName().toString();

        String b2Type = ((FieldDeclaration) b2).getType().toString();
        String b2VariableName = ((VariableDeclarationFragment) ((FieldDeclaration) b2).fragments().get(0)).getName().toString();

        return b1Type.equals(b2Type) && b1VariableName.equals(b2VariableName);
    }

    public static ClassChangeInfo[] compareBodyDeclarationList(List<BodyDeclaration> b1List, List<BodyDeclaration> b2List, String sameFile) {

        ClassChangeInfo[] changeInfos = new ClassChangeInfo[2];

        for (int i = 0; i < b1List.size(); i++) {

            BodyDeclaration b1 = b1List.get(i);

            for (int j = 0; j < b2List.size(); j++) {
                BodyDeclaration b2 = b2List.get(j);

                boolean compareResult = compareBodyDeclaration(b1, b2);

                if (compareResult) {
                    //如果比较得到相同的，从b1List,b2List中去掉
                    b1List.remove(i);
                    i--;
                    b2List.remove(j);
                    break;
                }
            }
        }
        ClassChangeInfo deleteClassChangeInfo = formatBodyDeclaration(b1List, sameFile);
        ClassChangeInfo addClassChangeInfo = formatBodyDeclaration(b2List, sameFile);

        changeInfos[0] = deleteClassChangeInfo;
        changeInfos[1] = addClassChangeInfo;

        return changeInfos;

    }

    /**
     * 按BodyDeclaration的类型分类
     *
     * @param fileName
     * @param bodyDeclarationList
     * @return
     */
    public static ClassChangeInfo formatBodyDeclaration(List<BodyDeclaration> bodyDeclarationList, String fileName) {

        ClassChangeInfo classChangeInfo = new ClassChangeInfo();

        MethodBean methodBean;
        VariableBean variableBean;

        List<MethodBean> methodBeanList = new ArrayList<>();
        List<VariableBean> variableBeanList = new ArrayList<>();

        for (BodyDeclaration bodyDeclaration : bodyDeclarationList) {
            if (bodyDeclaration instanceof MethodDeclaration) {

                String methodDeclarationName = ((MethodDeclaration) bodyDeclaration).getName().getIdentifier();
                List methodDeclarationParams = ((MethodDeclaration) bodyDeclaration).parameters();
                String bodyString = ((MethodDeclaration) bodyDeclaration).getBody() == null ? "" : ((MethodDeclaration) bodyDeclaration).getBody().toString();
                String returnType = ((MethodDeclaration) bodyDeclaration).getReturnType2() == null ? "" : ((MethodDeclaration) bodyDeclaration).getReturnType2().toString();
                List<String> params = convertParamsToStringList(methodDeclarationParams);
                methodBean = new MethodBean(methodDeclarationName, returnType, params, bodyString, fileName);
                methodBeanList.add(methodBean);
            } else if (bodyDeclaration instanceof FieldDeclaration) {
                String type = ((FieldDeclaration) bodyDeclaration).getType().toString();
                String variableName = ((VariableDeclarationFragment) ((FieldDeclaration) bodyDeclaration).fragments().get(0)).getName().toString();
                String property = ((VariableDeclarationFragment) ((FieldDeclaration) bodyDeclaration).fragments().get(0)).getInitializer() == null ? "" : ((VariableDeclarationFragment) ((FieldDeclaration) bodyDeclaration).fragments().get(0)).getInitializer().toString();

                variableBean = new VariableBean(type, variableName, property, fileName);
                variableBeanList.add(variableBean);
            }
        }
        classChangeInfo.setMethodBeanList(methodBeanList);
        classChangeInfo.setVariableBeanList(variableBeanList);

        return classChangeInfo;
    }

    /**
     * 将JDT中的参数列表转化为List<String>
     *
     * @param methodDeclarationParams
     * @return
     */
    private static List<String> convertParamsToStringList(List<SingleVariableDeclaration> methodDeclarationParams) {

        List<String> params = new ArrayList<>();

        for (SingleVariableDeclaration methodDeclarationParam : methodDeclarationParams) {
            params.add(methodDeclarationParam.getType().toString());
        }

        return params;
    }


}
