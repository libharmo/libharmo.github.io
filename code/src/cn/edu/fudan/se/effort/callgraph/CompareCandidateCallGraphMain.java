package cn.edu.fudan.se.effort.callgraph;

import cn.edu.fudan.se.effort.bean.MethodInJarResult;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.InvokeUtils4;
import cn.edu.fudan.se.util.JarUtil;
import cn.edu.fudan.se.util.JavaMethodUtil;
import com.google.gson.*;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 比较项目中每个方法在Jar包和候选Jar包中CallGraph
 */


public class CompareCandidateCallGraphMain {
    //private static final String INPUT_SOURCE = "C:\\Users\\Basti031\\Documents\\WeChat Files\\Schwein-neuer-1\\FileStorage\\File\\2019-05\\real_multi_version_method_calls_with_module\\real_multi_version_method_calls_with_module";
    private static final String INPUT_SOURCE = "C:\\Users\\Basti031\\Documents\\WeChat Files\\Schwein-neuer-1\\FileStorage\\File\\2019-05\\real_multi_version_method_calls_with_module\\2/";
    private static String JAR_DIR = "H:/wangying/lib_all/";
    private static String OUTPUT_DIR = "H:/shibowen/callgraph/buggyCallgraph2/";
    private static String METHOD_WARNING_DIR = "H:/shibowen/callgraph/methodInJarWarning/";
    private static String CALLGRAPH_WARNING_DIR = "H:/shibowen/callgraph/callGraphWarning/";
    private static String THIRD_PARTY_DIR = "H:/shibowen/callgraph/thirdPartyMethod/";


    //         filePath    jar        method    candidate     invokeMethod  info
    static Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> resultMap;


    private static final String RESULT_ADD = "add";
    private static final String RESULT_MODIFY = "modify";
    private static final String RESULT_DELETE = "delete";
    private static final String RESULT_SAME = "same";
    private static final String RESULT_NO_API = "no api";
    private static List<String> ERROR_FILE_List = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        initErrorList();

        File inputDir = new File(INPUT_SOURCE);
        File[] inputFiles = inputDir.listFiles();
        for (File file : inputFiles) {
            if (ERROR_FILE_List.contains(file.getName())) {
                continue;
            }

            file = new File("C:\\\\Users\\\\Basti031\\\\Documents\\\\WeChat Files\\\\Schwein-neuer-1\\\\FileStorage\\\\File\\\\2019-05\\\\real_multi_version_method_calls_with_module\\\\real_multi_version_method_calls_with_module/751.txt");

            /*File testFile = new File(OUTPUT_DIR + "/" + file.getName());
            if (testFile.exists()) {
                continue;
            }*/
            parseSingleProject(file);
        }


    }

    private static void initErrorList() {

        ERROR_FILE_List.add("143.txt");
        ERROR_FILE_List.add("130.txt");
    }

    private static void parseSingleProject(File file) throws Exception {

        resultMap = new HashMap<>();

        //文件内容
        JsonObject inputFileContent = readMapFile(file.getAbsolutePath());
        //项目Id
        String projectId = file.getName().split("\\.")[0];

        System.out.println("start parsing " + projectId);

        for (String filePath : inputFileContent.keySet()) {
            //filePath是java文件的路径
            //filePathInfo是对应的jar包和method列表

            //处理每个javaFile及即对应的value信息
            Map<String, Map<String, Map<String, Map<String, String>>>> jarMap = parseSingleFilePath(inputFileContent.getAsJsonObject(filePath), projectId, filePath);
            resultMap.put(filePath, jarMap);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtil.writeFlie(OUTPUT_DIR + projectId + ".txt", gson.toJson(resultMap));
        System.out.println("end parsing " + projectId);
    }

    /**
     * 处理每个javaFile及即对应的value信息
     *
     * @param filePathInfo
     * @param projectId
     * @throws Exception
     */
    private static Map<String, Map<String, Map<String, Map<String, String>>>> parseSingleFilePath(JsonObject filePathInfo, String projectId, String filePath) throws Exception {
        System.out.println("start parsing " + projectId + " " + filePath);
        Map<String, Map<String, Map<String, Map<String, String>>>> jarMap = new HashMap<>();
        for (String jarName : filePathInfo.keySet()) {
            if (jarName.equals("module")) {
                continue;
            }
            JsonArray methodList = filePathInfo.getAsJsonArray(jarName);
            //处理每一个单独的Jar包及其对应的Api方法列表
            Map<String, Map<String, Map<String, String>>> methodMap = parseSingeJar(jarName, methodList, projectId, filePath);
            if (methodMap != null) {
                jarMap.put(jarName, methodMap);
            }
        }
        System.out.println("end parsing " + projectId + " " + filePath);
        return jarMap;
    }


    private static Map<String, Map<String, Map<String, String>>> parseSingeJar(String jarName, JsonArray methodList, String projectId, String filePath) throws Exception {
        System.out.println("start parsing " + projectId + " " + filePath + " " + jarName);
        //只有当jar包符合要求以后才继续比较
        if (checkJarValid(jarName, filePath, projectId)) {

            String[] jarInfo = JavaMethodUtil.getJarInfo(jarName.split("\\.jar")[0]);
            if (jarInfo == null) {
                return null;
            }

            //候选jar包列表
            List<String> candidateJarVersions = JarUtil.getCandidateJarList(jarInfo[0], jarInfo[1]);

            //方法列表

            int methodSize = methodList.size();

            Map<String, Map<String, Map<String, String>>> methodMap = new HashMap<>();
            //遍历方法
            for (int methodIndex = 0; methodIndex < methodSize; methodIndex++) {
                String method = methodList.get(methodIndex).getAsString();
                //处理每个方法
                // candidate  invokeMethod info
                Map<String, Map<String, String>> candidateJarMap = parseSingleMethod(method, candidateJarVersions, jarName, filePath, projectId);
                if (candidateJarMap != null) {
                    methodMap.put(method, candidateJarMap);
                }
            }
            System.out.println("end parsing " + projectId + " " + filePath + " " + jarName);
            return methodMap;
        }
        System.out.println("end parsing " + projectId + " " + filePath + " " + jarName);
        return null;
    }

    /**
     * 处理每个方法
     *
     * @param method
     * @param candidateJarVersions
     * @param filePath
     * @param jarName
     */
    private static Map<String, Map<String, String>> parseSingleMethod(String method, List<String> candidateJarVersions, String jarName, String filePath, String projectId) throws Exception {
        System.out.println("start parsing " + projectId + " " + filePath + " " + jarName + " " + method);
        String[] jarInfo = JavaMethodUtil.getJarInfo(jarName.split("\\.jar")[0]);
        if (jarInfo == null) {
            return null;
        }
        //检查方法
        Map<String, Map<String, String>> candidateMap = null;
        int type = JavaMethodUtil.checkMethodType(method);
        if (checkMethodValid(method, type, jarName, filePath, projectId)) {

            //jar包存在，获取原始版本中该方法的callGraph
            System.out.println("========" + method);
            List<String> invokeMethodList = new ArrayList<>();
            if (type == JavaMethodUtil.METHOD_TYPE) {
                Map<String, List<String>> callGraph = InvokeUtils4.getInvokeMethodByMethodName(jarName, JAR_DIR + jarName, method);

                if (callGraph == null) {
                    //解析出错
                    FileUtil.appendLine(CALLGRAPH_WARNING_DIR + projectId + ".txt", "call graph generate error:" + jarName + method);
                    return null;
                }

                //callGraph中的所有方法
                invokeMethodList = InvokeUtils4.unique(callGraph);
            } else {
                //将变量加入invokeMethodList
                invokeMethodList.add(method);
            }

            candidateMap = new HashMap<>();
            //遍历候选Jar包
            for (String candidateJarVersion : candidateJarVersions) {
                String candidateJar = jarInfo[0] + "-" + candidateJarVersion + ".jar";
                //判断是否存在候选jar包
                if (JavaMethodUtil.jarExist(candidateJar)) {
                    Map<String, String> compareResultMap;
                    //如果jar包存在，则需要检查方法是否在jar包中存在
                    MethodInJarResult searchMethodCandidateResult = JavaMethodUtil.getMethodInJar(candidateJar, JAR_DIR, method, type, projectId, true, true);
                    if (searchMethodCandidateResult.getBodyString() == null) {
                        //如果bodyDeclaration为空，说明没有找到，原因在candidateResult.getResult()中
                        String result = searchMethodCandidateResult.getResult();
                        compareResultMap = generateNotExistMethodCompareMethod(method, result);

                    } else {
                        //候选jar包中存在该方法，
                        List<String> candidateMethodList = null;
                        if (type == JavaMethodUtil.METHOD_TYPE) {
                            //方法
                            Map<String, List<String>> candidateCallGraph = InvokeUtils4.getInvokeMethodByMethodName(candidateJar, JAR_DIR + candidateJar, method);
                            if (candidateCallGraph != null) {
                                candidateMethodList = InvokeUtils4.unique(candidateCallGraph);
                            } else {
                                continue;
                            }

                        } else {
                            candidateMethodList = new ArrayList<>();
                            candidateMethodList.add(method);
                        }
                        compareResultMap = compareInvokeMethodList(invokeMethodList, candidateMethodList, jarName, candidateJar, projectId);
                    }

                    candidateMap.put(candidateJar, compareResultMap);

                } else {
                    //jar包不存在
                    //暂定jar包不会不存在
                }
            }
        }
        System.out.println("end parsing " + projectId + " " + filePath + " " + jarName + " " + method);
        return candidateMap;
    }

    /**
     * 当RootMethod在新Jar包中不存在时，直接生成结果
     *
     * @param method
     * @param resultInfo
     * @return
     */
    private static Map<String, String> generateNotExistMethodCompareMethod(String method, String resultInfo) {
        Map<String, String> result = new HashMap<>();
        result.put(method, resultInfo);
        return result;
    }

    /**
     * 比较一个方法在两个不同版本Jar包中，调用的方法的情况
     *
     * @param invokeMethodList
     * @param candidateMethodList
     * @param jarName
     * @param candidateJar
     */
    private static Map<String, String> compareInvokeMethodList(List<String> invokeMethodList, List<String> candidateMethodList, String jarName, String candidateJar, String projectId) throws Exception {

        Map<String, String> methodResultMap = new HashMap<>();

        //交集，包含same和modify的
        List<String> unionList = new ArrayList<>(invokeMethodList);
        unionList.retainAll(candidateMethodList);

        //被删掉的方法列表
        List<String> deleteList = new ArrayList<>(invokeMethodList);
        deleteList.removeAll(candidateMethodList);

        //新添加的方法列表
        List<String> addList = new ArrayList<>(candidateMethodList);
        addList.removeAll(invokeMethodList);


        //判断unionList中的方法是same还是modify
        for (String unionMethod : unionList) {

            //先检测是什么类型（方法还是变量）
            int type = JavaMethodUtil.checkMethodType(unionMethod);

            System.out.println(unionMethod + " " + jarName + " " + candidateJar);
            boolean constructor = unionMethod.contains("<init>");
            MethodInJarResult methodInJarResult = JavaMethodUtil.getMethodInJar(jarName, JAR_DIR, unionMethod, type, projectId, true, true);
            MethodInJarResult candidateMethodInJarResult = JavaMethodUtil.getMethodInJar(candidateJar, JAR_DIR, unionMethod, type, projectId, true, true);

            if (methodInJarResult.getResult().equals(MethodInJarResult.METHOD_FROM_JDK) || candidateMethodInJarResult.getResult().equals(MethodInJarResult.METHOD_FROM_JDK)) {
                //方法来自JDK，忽略
                continue;
            }

            String body = methodInJarResult.getBodyString();
            String candidateBody = candidateMethodInJarResult.getBodyString();

            if (body == null && candidateBody == null) {
                //两个都是空，说明是三方库中被调用的方法,也有可能是找方法的代码有问题
                if (constructor) {
                    //如果是构造函数，且都没有找到，说明两个都是为声明默认的构造函数，即该方法在两个版本的Jar包中没有改变
                    methodResultMap.put(unionMethod, RESULT_SAME);
                } else {
                    FileUtil.appendLine(THIRD_PARTY_DIR + projectId + ".txt", jarName + " " + candidateJar + " " + unionMethod);
                }
                continue;
            } else if (body == null || candidateBody == null) {
                //有一个为空，说明在两个版本的jar包中该方法被删除或还未增加，因为在一个版本中该方法能找到，所以默认找方法的代码没有问题

                if (constructor) {
                    if (body == null) {
                        methodResultMap.put(unionMethod, RESULT_ADD);
                    } else {
                        methodResultMap.put(unionMethod, RESULT_DELETE);
                    }
                }

                continue;
            }

            if (body.equals(candidateBody)) {
                methodResultMap.put(unionMethod, RESULT_SAME);
            } else {
                methodResultMap.put(unionMethod, RESULT_MODIFY);
            }
        }

        for (String deleteMethod : deleteList) {
            methodResultMap.put(deleteMethod, RESULT_DELETE);
        }

        for (String addMethod : addList) {
            methodResultMap.put(addMethod, RESULT_ADD);
        }

        return methodResultMap;
    }

    /**
     * 检查方法是否符合要求
     *
     * @param method
     * @param type
     * @param jarName
     * @param filePath
     * @param projectId
     * @return
     */
    private static boolean checkMethodValid(String method, int type, String jarName, String filePath, String projectId) throws Exception {

        MethodInJarResult methodInJarResult = JavaMethodUtil.getMethodInJar(jarName, JAR_DIR, method, type, projectId, true, true);
        if (methodInJarResult.getBodyString() == null) {
            //没找到，出现异常，添加到项目的error文件
            FileUtil.appendLine(METHOD_WARNING_DIR + projectId + ".txt", "method not found in " + jarName + " : " + method);
            return false;
        }

        return true;
    }

    /**
     * 检查jar包是否符合要求
     * jarName 是jar包名字，其中包含了artifactName和version
     *
     * @param projectId
     * @param filePath
     * @param jarName
     * @return
     */
    private static boolean checkJarValid(String jarName, String filePath, String projectId) {

        if (jarName.equals("module")) {
            //不是jar包
            return false;
        }

        //找该jar包是否在本地存在，如果不存在跳过
        boolean jarExist = JavaMethodUtil.jarExist(jarName);
        if (!jarExist) {
            //jar包不存在
            return false;
        }


        return true;
    }

    /**
     * 读取项目文件
     *
     * @param filePath
     * @return
     */
    private static JsonObject readMapFile(String filePath) {

        String content = FileUtil.read(filePath);
        JsonObject returnData = new JsonParser().parse(content).getAsJsonObject();

        return returnData;
    }

    /**
     * 将body转为string
     *
     * @param body
     * @return
     */
    private static String convertBody2String(BodyDeclaration body) {

        Block block = ((MethodDeclaration) body).getBody();

        return block == null ? "" : block.toString();
    }


}