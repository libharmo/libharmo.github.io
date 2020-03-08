package cn.edu.fudan.se.effort.callgraph;

import cn.edu.fudan.se.effort.bean.MethodInJarResult;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.InvokeUtils4;
import cn.edu.fudan.se.util.JavaMethodUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuggyEffort2 {

    private static File[] inputFiles;
    private static Map<String, List<String>> candidateMap;
    private static String JAR_DIR = "H:/wangying/lib_all/";
    private static String OUTPUT_DIR = "H:/shibowen/callgraph/buggyCallgraph/";

    private static List<String> ERROR_JAR = new ArrayList<>();
    private static List<String> ERROR_FILE = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        initInputSource();

        handleCallGraph();

    }

    private static void handleCallGraph() throws Exception {
        //  filepath,  jar         candidate    method   invokeMethod info
        Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> resultMap;

        Map<String, Map<String, Map<String, Map<String, String>>>> jarMap;
        Map<String, Map<String, Map<String, String>>> candidateJarMap;
        Map<String, Map<String, String>> methodMap;
        for (File file : inputFiles) {
            System.out.println("开始解析文件" + file.getName());
            if (ERROR_FILE.contains(file.getName())) {
                continue;
            }
        /*    File testFile = new File(OUTPUT_DIR + file.getName());
            if (testFile.exists()) {
                continue;
            }*/

            resultMap = new HashMap<>();
            //文件内容
            JsonObject inputFileContent = readMapFile(file.getAbsolutePath());
            //项目Id
            String projectId = file.getName().split("\\.")[0];

            // jar          candidate    method     invokeMethod info
            Map<String, Map<String, Map<String, Map<String, String>>>> fileInfoMap;
            for (String filePath : inputFileContent.keySet()) {
                System.out.println("开始解析 " + file.getName() + " " + filePath);

                fileInfoMap = new HashMap<>();
                resultMap.put(filePath, fileInfoMap);
                JsonObject filePathInfo = inputFileContent.getAsJsonObject(filePath);
                //  candidate   method     invokeMethod info
                jarMap = new HashMap<>();
                for (String jarName : filePathInfo.keySet()) {
                    if (jarName.equals("module")) {
                        //不是jar包
                        continue;
                    }

                    if (ERROR_JAR.contains(jarName)) {
                        //有几个需要特殊处理的jar包
                        //todo
                        continue;
                    }
                    System.out.println("开始解析 " + file.getName() + " " + filePath + " " + jarName);

                    //首先找该jar包是否在本地存在，如果不存在跳过
                    boolean jarExist = JavaMethodUtil.jarExist(jarName);
                    if (!jarExist) {
                        //jar包不存在
                        //todo
                        continue;
                    }

                    String[] jarInfo = JavaMethodUtil.getJarInfo(jarName.split("\\.jar")[0]);

                    List<String> candidateJarVersions = JavaMethodUtil.getCandidates(candidateMap, jarInfo[0], jarInfo[1]);

        /*            //todo
                    if (candidateJarVersions.size() >= 2) {
                        candidateJarVersions = candidateJarVersions.subList(0, 2);
                    }*/


                    //方法列表
                    JsonArray methodList = filePathInfo.getAsJsonArray(jarName);
                    int methodSize = methodList.size();
              /*      //todo
                    if (methodSize >= 2) {
                        methodSize = 2;
                    }*/
                    //遍历方法
                    for (int methodIndex = 0; methodIndex < methodSize; methodIndex++) {

                        String method = methodList.get(methodIndex).getAsString();
                        //如果没有括号，说明是变量
                        if (!method.contains("(")) {
                            //不是方法
                            //todo
                            continue;
                        }
                        System.out.println("开始解析 " + file.getName() + " " + filePath + " " + jarName + " " + method);
                        //jar包存在，获取该方法的callGraph
                        Map<String, List<String>> callGraph = InvokeUtils4.getInvokeMethodByMethodName(jarName, JAR_DIR + jarName, method);
                        if (callGraph == null) {
                            //如果callGraph为空，这说明解析出错
                            //todo
                            continue;
                        }

                        //callGraph中调用的所有方法
                        List<String> uniqueMethodList = InvokeUtils4.unique(callGraph);

                 /*       //todo
                        if (uniqueMethodList.size() >= 2) {
                            uniqueMethodList = uniqueMethodList.subList(0, 2);
                        }*/


                        candidateJarMap = new HashMap<>();
                        for (String candidateJarVersion : candidateJarVersions) {
                            methodMap = new HashMap<>();
                            String candidateJarName = jarInfo[0] + "-" + candidateJarVersion + ".jar";
                            Map<String, String> invokeMap = new HashMap<>();
                            for (String uniqueMethod : uniqueMethodList) {
                                //找到方法在jar包中的实例
                                MethodInJarResult methodInJarResult = JavaMethodUtil.getMethodInJar(jarName, JAR_DIR, uniqueMethod, projectId);
                                if (methodInJarResult.getBodyDeclaration() == null) {
                                    //没有找到方法，出现问题（正常情况下应该能找到）
                                    //todo
                                    continue;
                                }
                                //获取uniqueMethod在Jar包中的实例
                                MethodInJarResult candidateResult = JavaMethodUtil.getMethodInJar(candidateJarName, JAR_DIR, uniqueMethod, projectId);

                                if (candidateResult.getBodyDeclaration() == null) {
                                    //没有找到方法，原因在candidateResult.getResult()中
                                    invokeMap.put(uniqueMethod, candidateResult.getResult());
                                } else {
                                    //找到方法，与原始jar包进行比较
                                    BodyDeclaration body = methodInJarResult.getBodyDeclaration();
                                    BodyDeclaration candidateBodyDeclaration = candidateResult.getBodyDeclaration();

                                    String bodyString = convertBody2String(body);
                                    String candidateBodyString = convertBody2String(candidateBodyDeclaration);

                                    if (bodyString.equals(candidateBodyString)) {
                                        invokeMap.put(uniqueMethod, "same");

                                    } else {
                                        //不同
                                        invokeMap.put(uniqueMethod, "modify");
                                    }
                                }
                            }
                           /* methodMap.put(method, invokeMap);


                            if (!candidateJarMap.containsKey(candidateJarName)) {
                                candidateJarMap.put(candidateJarName, new HashMap<>());
                            }
                            candidateJarMap.get(candidateJarName).put(method, invokeMap);*/

                            if (!resultMap.containsKey(filePath)) {
                                resultMap.put(filePath, new HashMap<>());
                            }
                            if (!resultMap.get(filePath).containsKey(jarName)) {
                                resultMap.get(filePath).put(jarName, new HashMap<>());
                            }

                            if (!resultMap.get(filePath).get(jarName).containsKey(candidateJarName)) {
                                resultMap.get(filePath).get(jarName).put(candidateJarName, new HashMap<>());
                            }

                            if (!resultMap.get(filePath).get(jarName).get(candidateJarName).containsKey(method)) {
                                resultMap.get(filePath).get(jarName).get(candidateJarName).put(method, new HashMap<>());
                            }

                            resultMap.get(filePath).get(jarName).get(candidateJarName).put(method, invokeMap);


                        }
                        //jarMap.put(jarName, candidateJarMap);

                      /*  for (Map.Entry<String, Map<String, Map<String, String>>> candidateMapEntry : candidateJarMap.entrySet()) {

                            String candidateName = candidateMapEntry.getKey();

                            if (!jarMap.containsKey(jarName)) {
                                jarMap.put(jarName, new HashMap<>());
                            }
                            //jarMap.get(jarName).get(candidateName).put()

                        }
*/
                        System.out.println("结束解析 " + file.getName() + " " + filePath + " " + jarName + " " + method);
                    }
                    // resultMap.put(filePath, jarMap);
                    System.out.println("结束解析 " + file.getName() + " " + filePath + " " + jarName);
                }
                System.out.println("结束解析 " + file.getName() + " " + filePath);
            }
            FileUtil.writeFlie(OUTPUT_DIR + file.getName(), new Gson().toJson(resultMap));

            System.out.println("结束解析文件" + file.getName());
        }
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

    /**
     * 初始化
     */
    private static void initInputSource() {

        //读取以一个项目为一个文件的输入源
        inputFiles = getAllInputFiles();

        //读取候选jar包
        candidateMap = getCandidateMap();

        initErrorJars();
        initErrorFiles();


    }


    private static void initErrorFiles() {
//        ERROR_FILE.add("130.txt");
//        ERROR_FILE.add("236.txt");
//        ERROR_FILE.add("941.txt");
    }

    /**
     * 特殊处理
     */
    private static void initErrorJars() {
        ERROR_JAR.add("ganymed-ssh2-build210.jar");
        ERROR_JAR.add("ganymed-ssh2-build209.jar");
        ERROR_JAR.add("jtidy-r938.jar");
        ERROR_JAR.add("guava-r05.jar");
        ERROR_JAR.add("jdk7-1.9.1.jar");
        ERROR_JAR.add("janusgraph-hadoop-core-0.3.0-20180730.212702-19.jar");
        ERROR_JAR.add("janusgraph-test-0.3.0-20180730.212123-19.jar");
        ERROR_JAR.add("cglib-nodep-2.2.jar");
        ERROR_JAR.add("jdbi-2.63.1.jar");
        ERROR_JAR.add("hadoop-common-3.0.0.jar");

    }

    private static Map<String, List<String>> getCandidateMap() {
        String content = FileUtil.read("input/all_candidates.txt");

        Map<String, List<String>> candidateMap = new Gson().fromJson(content, new TypeToken<Map<String, List<String>>>() {
        }.getType());

        return candidateMap;
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
     * 得到所有的输入文件
     *
     * @return
     */
    private static File[] getAllInputFiles() {

        //File inputDir = new File("C:\\Users\\Basti031\\Documents\\WeChat Files\\Schwein-neuer-1\\FileStorage\\File\\2019-05\\real_multi_version_method_calls_with_module\\real_multi_version_method_calls_with_module");
        File inputDir = new File("D:\\cs\\test");
        return inputDir.listFiles();
    }

}
