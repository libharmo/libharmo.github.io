package cn.edu.fudan.se.effort.callgraph;


import cn.edu.fudan.se.effort.bean.*;
import cn.edu.fudan.se.effort.m_interface.IMethodCompare;
import cn.edu.fudan.se.effort.m_interface.IVariableCompare;
import cn.edu.fudan.se.effort.m_interface.impl.MethodCompareImpl1;
import cn.edu.fudan.se.effort.m_interface.impl.VariableCompareImpl1;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JarUtil;
import cn.edu.fudan.se.util.JavaMethodUtil;
import cn.edu.fudan.se.util.MethodCompareUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 推荐新版本方法
 */
public class RecommendUtils {

    private static final String JAVA_DOC_DIR = "H:/shibowen/lib_javadoc/lib/";
    private static final String JAVA_DOC_RESULT_DIR = "H:/shibowen/javadoc_result/";
    private static final String JAR_COMPARE_OUTPUT_DIR = "H:\\shibowen\\method_level_compare/";
    private static final String DELETE_JAR_FILE_PATH = "input/deleted_method_jar.json";
    static List<String> jarList;
    static IMethodCompare iMethodCompare = new MethodCompareImpl1();
    static IVariableCompare iVariableCompare = new VariableCompareImpl1();

    /**
     * 推荐
     *
     * @param artifactId
     * @param method
     * @param originVersion
     * @param recommendVersion
     */
    public static Object recommendMethod(String artifactId, String method, String originVersion, String recommendVersion) throws FileNotFoundException {
        Map<String, String> javaDocRecommendMap = recommendByJavaDoc(artifactId, method, originVersion, recommendVersion);
        if (javaDocRecommendMap != null) {
            //如果javadoc推荐不为空，推荐成功
            return javaDocRecommendMap;
        } else {
            Serializable result = recommendByMethodCompare(artifactId, method, originVersion, recommendVersion);
            return result;
        }
    }

    private static Serializable recommendByMethodCompare(String artifactId, String method, String originVersion, String recommendVersion) throws FileNotFoundException {

        MethodLevelCompare methodLevelCompare = getMethodCompareResult(artifactId, originVersion, recommendVersion);
        int type = JavaMethodUtil.checkMethodType(method);
        Map<String, ClassChangeInfo> addMethodMap = methodLevelCompare.getAddMethodMap();
        if (type == JavaMethodUtil.METHOD_TYPE) {
            MethodBean methodBean = getMethodBean(method, methodLevelCompare.getDeleteMethodMap());
            if (methodBean == null) {
//                System.err.println(artifactId + " " + method + " " + originVersion + " " + recommendVersion);
                return null;
            }
            MethodCompareUtil.getInstance(iMethodCompare, iVariableCompare);
            List<MethodBean> methodBeanList = MethodCompareUtil.compareMethods(methodBean, addMethodMap);
            MethodBeanPair methodBeanPair = new MethodBeanPair(methodBean, methodBeanList);
            return methodBeanPair;

        } else {
            //todo
            VariableBean variableBean = getVariableBean(method, methodLevelCompare.getDeleteMethodMap());
            if (variableBean == null) {
//                System.out.println(artifactId + " " + method + " " + originVersion + " " + recommendVersion);
                return null;
            }
            MethodCompareUtil.getInstance(iMethodCompare, iVariableCompare);

            List<VariableBean> variableBeanList = MethodCompareUtil.compareVariable(variableBean, addMethodMap);
            VariableBeanPair variableBeanPair = new VariableBeanPair(variableBean, variableBeanList);
            return variableBeanPair;
        }


    }

    private static VariableBean getVariableBean(String method, Map<String, ClassChangeInfo> deleteMethodMap) {

        String variableName = JavaMethodUtil.getVariableName(method);
        String javaFile = JavaMethodUtil.getVariableClass(method).replaceAll("\\$", ".");

        for (Map.Entry<String, ClassChangeInfo> deleteMapEntry : deleteMethodMap.entrySet()) {
            String filePath = deleteMapEntry.getKey();
            ClassChangeInfo classChangeInfo = deleteMapEntry.getValue();

            List<VariableBean> variableBeanList = classChangeInfo.getVariableBeanList();

            for (VariableBean variableBean : variableBeanList) {
                String variableBeanName = variableBean.getVariableName();
                //"/javax/servlet/MultipartConfigElement.java"
                String fileName = variableBean.getFileName().replaceAll("/", ".");

                if (!variableName.equals(variableName)) {
                    continue;
                }


                if (!fileName.contains(javaFile)) {
                    continue;
                }
                return variableBean;
            }

        }

        return null;
    }

    /**
     * @param method
     * @param deleteMethodMap
     * @return
     */
    private static MethodBean getMethodBean(String method, Map<String, ClassChangeInfo> deleteMethodMap) {

        String methodName = JavaMethodUtil.getMethodName(method);
        List<String> params = JavaMethodUtil.getParamNames(method);
        String javaFile = JavaMethodUtil.getMethodClass(method).replaceAll("\\$", ".");

        for (Map.Entry<String, ClassChangeInfo> deleteMapEntry : deleteMethodMap.entrySet()) {
            String filePath = deleteMapEntry.getKey();
            ClassChangeInfo classChangeInfo = deleteMapEntry.getValue();

            List<MethodBean> methodBeanList = classChangeInfo.getMethodBeanList();

            for (MethodBean methodBean : methodBeanList) {
                String methodBeanName = methodBean.getMethodName();
                List<String> methodBeanParams = methodBean.getParams();
                //"/javax/servlet/MultipartConfigElement.java"
                String fileName = methodBean.getFileName().replaceAll("/", ".");

                if (!methodName.equals(methodBeanName)) {
                    continue;
                }
                if (methodBeanParams.size() != params.size()) {
                    continue;
                }

                if (!fileName.contains(javaFile)) {
                    continue;
                }

                int size = methodBeanParams.size();

                for (int i = 0; i < size; i++) {
                    String param = params.get(i);
                    String methodBeanParam = methodBeanParams.get(i);

                    if (!param.equals(method) && !param.endsWith(methodBeanParam) && !methodBeanParam.endsWith(param)) {
                        continue;
                    }
                }
                return methodBean;
            }

        }

        return null;
    }

    private static MethodLevelCompare getMethodCompareResult(String artifactId, String originVersion, String recommendVersion) throws FileNotFoundException {

        MethodLevelCompare methodLevelCompare = null;
        String resultFileName = artifactId + "__fdse__" + originVersion + "__fdse__" + recommendVersion + ".json";
        String resultFilePath = JAR_COMPARE_OUTPUT_DIR + resultFileName;
        if (new File(resultFilePath).exists()) {
            methodLevelCompare = new Gson().fromJson(FileUtil.read(resultFilePath), new TypeToken<MethodLevelCompare>() {
            }.getType());
            return methodLevelCompare;
        }


        methodLevelCompare = JarUtil.compareJar("H:/wangying/lib_all", artifactId, originVersion, recommendVersion);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileUtil.writeFlie(resultFilePath, gson.toJson(methodLevelCompare));


        return methodLevelCompare;
    }

    public static void main(String[] args) {
        Map<String, String> result = recommendByJavaDoc("arangodb-java-driver", "com.arangodb.ArangoDriver.graphGetVertexCursor(String, Class, Object, GraphVerticesOptions, AqlQueryOptions)",
                "1", "2");


    }


    private static Map<String, String> recommendByJavaDoc(String artifactId, String method, String originVersion, String recommendVersion) {
        int type = JavaMethodUtil.checkMethodType(method);
        List<String> versionList = getAllVersions(artifactId, originVersion, recommendVersion);
        for (String version : versionList) {
            String javaDocResultPath = JAVA_DOC_RESULT_DIR + version;

            Map<String, Map<String, Map<String, String>>> recommendMap;

            String content = FileUtil.read(javaDocResultPath);
            recommendMap = new Gson().fromJson(content, new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
            }.getType());
            Map<String, String> result = null;
            for (Map.Entry<String, Map<String, Map<String, String>>> recommendMapEntry : recommendMap.entrySet()) {

                String key = recommendMapEntry.getKey();
//                有几个方法key需要特殊处理
                if (key.equals("Methods") || key.equals("Constructors") || key.equals("Deprecated Methods") || key.equals("Deprecated Constructors")) {
                    //如果是这几个key，需要从方法名、参数列表比较
                    if (type == JavaMethodUtil.METHOD_TYPE) {
                        result = checkMethod(recommendMapEntry.getValue(), method);
                    }
                } else {
                    //除此之外，其他的只要比较前缀即可
                    result = checkStartWith(recommendMapEntry.getValue(), method);
                }

                if (result != null) {
                    //找到要代替的方法
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 检查方法是否匹配
     *
     * @param value
     * @param method
     * @return
     */
    private static Map<String, String> checkMethod(Map<String, Map<String, String>> value, String method) {


        for (Map.Entry<String, Map<String, String>> entry : value.entrySet()) {
            String methodKey = entry.getKey();
            boolean same = JavaMethodUtil.compare2Method(method, methodKey);

            if (same) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 检查特殊key下是否包含特定的方法
     *
     * @param valueMap
     * @param method
     */
    private static Map<String, String> checkStartWith(Map<String, Map<String, String>> valueMap, String method) {

        for (Map.Entry<String, Map<String, String>> methodEntry : valueMap.entrySet()) {
            String methodKey = methodEntry.getKey();
            if (method.contains(methodKey)) {
                return methodEntry.getValue();
            }
        }

        return null;
    }

    /**
     * @param artifactId
     * @param originVersion
     * @param recommendVersion
     * @return
     */
    private static List<String> getAllVersions(String artifactId, String originVersion, String recommendVersion) {

        List<String> versionJarList = new ArrayList<>();

        if (jarList == null) {
            jarList = new ArrayList<>();
            File[] fileList = new File(JAVA_DOC_RESULT_DIR).listFiles();
            for (File file : fileList) {
                jarList.add(file.getName());
            }
        }


        for (String jarDocResultFile : jarList) {
            if (jarDocResultFile.startsWith(artifactId)) {
                versionJarList.add(jarDocResultFile);
            }
        }


        return versionJarList;
    }

}
