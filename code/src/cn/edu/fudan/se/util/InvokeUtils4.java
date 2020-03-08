package cn.edu.fudan.se.util;

import cn.edu.fudan.se.cfg.rq2.bean.DependencyItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gr.gousiosg.javacg.stat.CheckMapResult;
import gr.gousiosg.javacg.stat.ClassVisitor;
import org.apache.bcel.classfile.ClassParser;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CallGraph相关类
 */
public class InvokeUtils4 {
/*    static private String JAVA_CALLGRAPH_CACHE_DIR = "D:/wangying/cg_cache/";
    static private String JAVA_SUPERCLASS_CACHE_DIR = "D:/wangying/sp_cache/";*/

    static private String JAVA_CALLGRAPH_CACHE_DIR = "H:/shibowen/callgraph/cache/cg_cache/";
    static private String JAVA_SUPERCLASS_CACHE_DIR = "H:/shibowen/callgraph/cache/sp_cache/";
    static private String JAVA_ERROR = "H:/shibowen/callgraph/cache/error/";
    static GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    static private Gson gson = gsonBuilder.create();

    static {
        File file = new File(JAVA_CALLGRAPH_CACHE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(JAVA_SUPERCLASS_CACHE_DIR);
        if (!file2.exists()) {
            file2.mkdirs();
        }
    }

    /**
     * 读取缓存
     *
     * @param jarName
     * @return
     */
    private static Map<String, List<String>> getCGCache(String jarName) {
        String jarCacheFilePath = JAVA_CALLGRAPH_CACHE_DIR + jarName + ".txt";
        File cacheFile = new File(jarCacheFilePath);

        String errorCacheFile = JAVA_ERROR + '/' + jarName + ".txt";
        if (new File(errorCacheFile).exists()) {
            return new HashMap<>();
        }

        if (!cacheFile.exists()) {
            return null;
        }

        String cache = FileUtil.read(cacheFile.getAbsolutePath());
        Map<String, List<String>> cacheMap = new HashMap<>();
        cacheMap = gson.fromJson(cache, cacheMap.getClass());
        return cacheMap;
    }

    /**
     * 读取jar包中类的继承关系缓存
     *
     * @param jarName
     * @return
     */
    public static Map<String, String> getSPCache(String jarName) {
        String jarCacheFilePath = JAVA_SUPERCLASS_CACHE_DIR + jarName + ".txt";
        File cacheFile = new File(jarCacheFilePath);
        if (!cacheFile.exists()) {
            return null;
        }

        String cache = FileUtil.read(cacheFile.getAbsolutePath());
        Map<String, String> cacheMap = new HashMap<>();
        cacheMap = gson.fromJson(cache, cacheMap.getClass());
        return cacheMap;
    }

    /**
     * @param dependencyItemList
     * @return
     * @throws Exception
     * @deprecated
     */
    public static Map<String, Map<String, List<String>>> generateInvokeMaps(List<DependencyItem> dependencyItemList) throws Exception {

        Map<String, Map<String, List<String>>> resultMap = new HashMap<>();

        for (DependencyItem dependencyItem : dependencyItemList) {
            Map<String, List<String>> map = generateInvokeMap(dependencyItem.getJarName(), dependencyItem.getJarPath());
            resultMap.put(dependencyItem.toString(), map);
        }
        return resultMap;

    }

    /**
     * 生成jar包所有的调用关系
     *
     * @param jarName
     * @param jarPath
     * @return 如果返回null，说明解析出错
     * @throws Exception
     */
    public static Map<String, List<String>> generateInvokeMap(String jarName, String jarPath) throws Exception {
        Map<String, List<String>> invokeMap = getCGCache(jarName);

        if (invokeMap != null) {
            return invokeMap;
        }
        String[] invokeStringList;
        try {
            invokeStringList = getJarInvokeMethodList(jarPath, jarName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (invokeStringList == null) {
            return null;
            //throw new Exception("error invoke string list!");
        } else {
            invokeMap = generateJarInvokeListToMap(invokeStringList);
        }

        FileUtil.writeFlie(JAVA_CALLGRAPH_CACHE_DIR + jarName + ".txt", gson.toJson(invokeMap));
        //FileUtil.writeFlie("D://" + jarName + ".txt", gson.toJson(invokeStringList));
        return invokeMap;
    }

    /**
     * 指定路径生成Callgraph的缓存文件
     *
     * @param jarName
     * @param jarPath
     * @param cacheDir
     * @return
     * @throws Exception
     */
    public static String generateInvokeMapCacheFile(String jarName, String jarPath, String cacheDir) throws Exception {
        Map<String, List<String>> invokeMap;
        String[] invokeStringList = getJarInvokeMethodList(jarPath, jarName);
        if (invokeStringList == null) {
            throw new Exception("error invoke string list!");
        } else {
            invokeMap = generateJarInvokeListToMap(invokeStringList);
        }

        FileUtil.writeFlie(cacheDir + "/" + jarName + ".txt", gson.toJson(invokeMap));
        return cacheDir + "/" + jarName + ".txt";
    }

    /**
     * 得到以指定方法为根节点的调用关系
     *
     * @param jarName
     * @param jarPath
     * @param methodName
     * @return
     * @throws Exception
     */
    public static Map<String, List<String>> getInvokeMethodByMethodName(String jarName, String jarPath, String methodName) throws Exception {
        //之前预备工作的到的map，存储所有方法
        Map<String, List<String>> jarInvokeMethodMap = generateInvokeMap(jarName, jarPath);
        if (jarInvokeMethodMap == null) {
            return null;
        }
        //最后结果
        Map<String, List<String>> resultInvokeMethodMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(methodName);

        while (!queue.isEmpty()) {
            String method = queue.poll();
            //首先看是否是jdk内的方法
            //    "generics.GenericsMain.printArray(java.lang.Object[])",
            //"inner_method.MainInner$InnerClass.testInner()"

            if (method.contains("java.security.NoSuchAlgorithmException.")) {
                int a = 1;
            }

            String classPath = JavaMethodUtil.getMethodClass(method);

            if (JavaMethodUtil.isJDKMethod(classPath)) {
                //jdk方法
                continue;
            }


            CheckMapResult checkMapResult = checkJarInvokeMethodMapContains(jarInvokeMethodMap, method);
            if (checkMapResult.getSuccess()) {
                //if (jarInvokeMethodMap.containsKey(method)) {
                List<String> invokeMethodList = jarInvokeMethodMap.get(checkMapResult.getMethodString());
                if (resultInvokeMethodMap.containsKey(checkMapResult.getMethodString())) {
                    //已经加过，不用再处理
                } else {
                    //如果该方法还没加过，加入key-value，且把调用的方法加进queue
                    resultInvokeMethodMap.put(checkMapResult.getMethodString(), new ArrayList<>());
                    for (String invokeMethod : invokeMethodList) {

                        String invokeClassPath = JavaMethodUtil.getMethodClass(invokeMethod);

                        if (JavaMethodUtil.isJDKMethod(invokeClassPath)) {
                            //jdk方法
                            continue;
                        }

                        resultInvokeMethodMap.get(checkMapResult.getMethodString()).add(invokeMethod);
                        queue.offer(invokeMethod);
                    }
                }
            } else {
                //System.out.println("++++"+method);
            }
        }

        //如果本身没有调用方法，要将其本身加入其中
        if (resultInvokeMethodMap.size() == 0) {
            resultInvokeMethodMap.put(methodName, new ArrayList<>());
        }

        return resultInvokeMethodMap;
    }

    /**
     * 得到指定方法的调用关系,包含依赖的jar包
     *
     * @param jarName
     * @param jarPath
     * @param methodName
     * @return
     * @throws Exception
     * @deprecated
     */
    public static Map<String, List<String>> getInvokeMethodByMethodName(String jarName, String jarPath, String methodName, List<DependencyItem> dependencyItemList) throws Exception {
        //之前预备工作的到的map，存储所有方法
        Map<String, List<String>> jarInvokeMethodMap = generateInvokeMap(jarName, jarPath);
        //最后结果
        Map<String, List<String>> resultInvokeMethodMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(methodName);

        while (!queue.isEmpty()) {
            String method = queue.poll();
            //首先看是否是jdk内的方法
            //    "generics.GenericsMain.printArray(java.lang.Object[])",
            //"inner_method.MainInner$InnerClass.testInner()"

            String classPath = JavaMethodUtil.getMethodClass(method);

            if (JavaMethodUtil.isJDKMethod(classPath)) {
                //jdk方法
                continue;
            }


            CheckMapResult checkMapResult = checkJarInvokeMethodMapContains(jarInvokeMethodMap, method);
            if (checkMapResult.getSuccess()) {
                //如果找到这个方法，说明是在自身jar包内
                //if (jarInvokeMethodMap.containsKey(method)) {
                List<String> invokeMethodList = jarInvokeMethodMap.get(checkMapResult.getMethodString());
                if (resultInvokeMethodMap.containsKey(checkMapResult.getMethodString())) {
                    //已经加过，不用再处理
                } else {
                    //如果该方法还没加过，加入key-value，且把调用的方法加进queue
                    resultInvokeMethodMap.put(checkMapResult.getMethodString(), new ArrayList<>());
                    for (String invokeMethod : invokeMethodList) {
                        resultInvokeMethodMap.get(checkMapResult.getMethodString()).add(invokeMethod);
                        queue.offer(invokeMethod);
                    }
                }
            } else {
                //如果没有找到方法，说明1.在自身jar包中，但没有调用其他方法（忽略）2.在依赖的子jar包中
                //遍历dependencyItemList，在其对应的jar包中搜索方法

                for (DependencyItem dependencyItem : dependencyItemList) {
                    String childJarName = dependencyItem.getJarName();
                    String childJarPath = dependencyItem.getJarPath();
                    Map<String, List<String>> childInvokeMaps = InvokeUtils4.generateInvokeMap(childJarName, childJarPath);
                    if (childInvokeMaps.containsKey(method)) {
                        //说明是在子jar包内的方法
                        dependencyItem.getMethodList().add(method);
                    }
                }
            }
        }

        return resultInvokeMethodMap;
    }

    /**
     * 检查某一个Callgraph中是否含有某个方法
     *
     * @param jarInvokeMethodMap
     * @param method
     * @return
     */
    private static CheckMapResult checkJarInvokeMethodMapContains(Map<String, List<String>> jarInvokeMethodMap, String method) {
        boolean constructor = JavaMethodUtil.isConstructor(method);
        if (constructor) {
            method = JavaMethodUtil.formatConstructor(method);
        }
        if (jarInvokeMethodMap.containsKey(method)) {
            return new CheckMapResult(method, true);
        }
        String s = method;
        int index2 = s.toString().indexOf('(');
        if (index2 == -1) {
            return new CheckMapResult(method, false);
        }

        String subS = s.substring(0, index2);
        int index = subS.lastIndexOf('.');
        String className = s.substring(0, index);
        String methodName = s.substring(index + 1, index2);
        String params = s.substring(index2);
        //initClassNames(classNames, className);
        while (true) {
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex == -1) {
                return new CheckMapResult("", false);
            }
            StringBuilder classNameSb = new StringBuilder(className);
            classNameSb.replace(dotIndex, dotIndex + 1, "$");
            className = classNameSb.toString();
            String totalMethodName = className + "." + methodName + params;
            if (jarInvokeMethodMap.containsKey(totalMethodName)) {
                return new CheckMapResult(totalMethodName, true);
            }
        }
    }


    /**
     * 将调用关系的String[]转化为Map
     *
     * @param invokeStringList
     * @return
     */
    private static Map<String, List<String>> generateJarInvokeListToMap(String[] invokeStringList) {
        Map<String, List<String>> resultList = new HashMap<>();
        for (String invokeString : invokeStringList) {
            if (invokeString.startsWith("M:")) {
                String[] invokeStrings = invokeString.split(" ");
                String callerMethodString = invokeStrings[0].split("M:")[1].replace(":", ".");
                String invokeMethodString = invokeStrings[1].replace(":", ".").substring(3);
                if (!resultList.containsKey(callerMethodString)) {
                    resultList.put(callerMethodString, new ArrayList<>());
                }
                resultList.get(callerMethodString).add(invokeMethodString);
            }
        }
        return resultList;
    }

    /**
     * 得到jar包的调用关系String[]
     *
     * @param jarPath
     * @param jarName
     * @return
     */
    public static String[] getJarInvokeMethodList(String jarPath, String jarName) {
        /**
         * lambda表达式
         * 传入参数ClassParser cp
         * 返回ClassVisitor classVisitor
         */
        Function<ClassParser, ClassVisitor> getClassVisitor =
                (ClassParser cp) -> {
                    try {
                        return new ClassVisitor(cp.parse());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };

        try {

            File f = new File(jarPath);

            if (!f.exists()) {
                System.err.println("Jar file " + jarPath + " does not exist");
                return null;
            }

            try (JarFile jar = new JarFile(f)) {

                //是否需要缓存子类父类关系图
                boolean shouldCacheSuperClass = true;
                File cpCache = new File(JAVA_SUPERCLASS_CACHE_DIR + jarName + ".txt");
                if (cpCache.exists()) {
                    shouldCacheSuperClass = false;
                }


                Enumeration jarEntries = jar.entries();
                Stream<JarEntry> entries = enumerationAsStream(jarEntries);
                Map<String, String> classSuperMap = new HashMap<>();
                boolean finalShouldCacheSuperClass = shouldCacheSuperClass;
                String methodCalls = null;
                try {
                    methodCalls = entries.
                            flatMap(e -> {
                                if (e.isDirectory() || !e.getName().endsWith(".class"))
                                    return (new ArrayList<String>()).stream();
                                ClassParser cp = new ClassParser(jarPath, e.getName());

                                ClassVisitor classVisitor = getClassVisitor.apply(cp).start();

                                Stream<String> result = classVisitor.methodCalls().stream();
                                String currentClass = e.getName();
                                if (finalShouldCacheSuperClass) {
                                    String superClassName = classVisitor.getSuperClassName();
                                    currentClass = currentClass.substring(0, currentClass.length() - 6).replaceAll("/", ".");
                                    classSuperMap.put(currentClass, superClassName);
                                }
                                return result;
                            }).
                            map(s -> s + "\n").
                            reduce(new StringBuilder(),
                                    StringBuilder::append,
                                    StringBuilder::append).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (shouldCacheSuperClass) {
                    FileUtil.writeFlie(JAVA_SUPERCLASS_CACHE_DIR + jarName + ".txt", new Gson().toJson(classSuperMap));
                }
                if (methodCalls == null) {
                    return null;
                } else {
                    return methodCalls.split("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }

        return null;

    }

    private static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }


    /**
     * 链式依赖
     *
     * @param prevJar
     * @param jar
     * @param methodName
     * @param invokeMaps
     * @return
     */
    public static Map<String, List<String>> getInvokeMethodByMethodName(String prevJar, String jar, String methodName, Map<String, Map<String, List<String>>> invokeMaps) {
        //当前查看的jar包groupId artifactId groupId 形式
        //key为 grourId:artifactId:version com.xxx.yyy.zzz.method() 的形式
        Map<String, List<String>> resultInvokeMethodMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(methodName);

        while (!queue.isEmpty()) {
            String method = queue.poll();
            CheckMapResult checkMapResult = checkChainInvokeMap(invokeMaps, method);
            if (checkMapResult.getSuccess()) {
                //if (jarInvokeMethodMap.containsKey(method)) {
                List<String> invokeMethodList = invokeMaps.get(checkMapResult.getMethodString()).get("");
                if (resultInvokeMethodMap.containsKey(checkMapResult.getMethodString())) {
                    //已经加过，不用再处理
                } else {
                    //如果该方法还没加过，加入key-value，且把调用的方法加进queue
                    resultInvokeMethodMap.put(checkMapResult.getMethodString(), new ArrayList<>());
                    for (String invokeMethod : invokeMethodList) {
                        resultInvokeMethodMap.get(checkMapResult.getMethodString()).add(invokeMethod);
                        queue.offer(invokeMethod);
                    }
                }
            } else {
                //System.out.println("++++"+method);
            }
        }

        return resultInvokeMethodMap;


    }

    /**
     * 在链式调用中查找方法调用
     *
     * @param invokeMaps
     * @param method
     * @return
     */
    private static CheckMapResult checkChainInvokeMap(Map<String, Map<String, List<String>>> invokeMaps, String method) {
        return null;
      /*  boolean constructor = JavaMethodUtil.isConstructor(method);
        if (constructor) {
            method = JavaMethodUtil.formatConstructor(method);
        }
        for (Map.Entry<String, Map<String, List<String>>> invokeMapEntry : invokeMaps.entrySet()) {
            Map<String, List<String>> map = invokeMapEntry.getValue();
            if (map.containsKey(method))

        }

        if (jarInvokeMethodMap.containsKey(method)) {
            return new CheckMapResult(method, true);
        }
        String s = method;
        int index2 = s.indexOf('(');
        if (index2 == -1) {
            return new CheckMapResult(method, false);
        }

        String subS = s.substring(0, index2);
        int index = subS.lastIndexOf('.');
        String className = s.substring(0, index);
        String methodName = s.substring(index + 1, index2);
        String params = s.substring(index2);
        //initClassNames(classNames, className);
        while (true) {
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex == -1) {
                return new CheckMapResult("", false);
            }
            StringBuilder classNameSb = new StringBuilder(className);
            classNameSb.replace(dotIndex, dotIndex + 1, "$");
            className = classNameSb.toString();
            String totalMethodName = className + "." + methodName + params;
            if (jarInvokeMethodMap.containsKey(totalMethodName)) {
                return new CheckMapResult(totalMethodName, true);
            }
        }*/
    }


    public static Map<String, List<String>> formatCallGraph(Map<String, List<String>> callGraph, String method) {

        /**
         * 如果没有调用其他方法，需要将方法本身加入其中
         */

        if (!callGraph.containsKey(method)) {
            callGraph.put(method, new ArrayList<>());
        }

        for (Map.Entry<String, List<String>> mapEntry : callGraph.entrySet()) {


        }

        String classPath = JavaMethodUtil.getMethodClass(method);

  /*      if (JavaMethodUtil.isJDKMethod(classPath)) {
            //jdk方法
            continue;
        }
*/
        return null;

    }

    /**
     * 规范化方法名
     *
     * @param method
     * @return
     */
    public static String formatMethod(String method) {

        //去除空格
        method = method.replaceAll(" ", "");

        return method;
    }

    /**
     * callGraph去重
     *
     * @param callGraph
     * @return
     */
    public static List<String> unique(Map<String, List<String>> callGraph) {
        Set<String> methodSet = new HashSet<>();

        for (Map.Entry<String, List<String>> methodEntry : callGraph.entrySet()) {

            String rootMethod = methodEntry.getKey();
            List<String> invokeMethods = methodEntry.getValue();

            methodSet.add(rootMethod);

            for (String method : invokeMethods) {
                methodSet.add(method);
            }
        }
        return new ArrayList<>(methodSet);
    }


    /**
     * 获取当前类的父类Map
     *
     * @param jarName
     * @param jarLib
     */
    public static Map<String, String> getSuperClassMap(String jarName, String jarLib) throws Exception {
        Map<String, String> superClassMap = getSPCache(jarName);
        if (superClassMap != null) {
            return superClassMap;
        }

        generateInvokeMap(jarName, jarLib + "/" + jarName);
        superClassMap = getSPCache(jarName);

        return superClassMap;
    }

    public static String getSuperClassName(String jarName, String jarLib, String currentClassName) throws Exception {

        Map<String, String> superClassMap = getSuperClassMap(jarName, jarLib);

        /**
         * 没有找到缓存，且生成也有错误
         */
        if (superClassMap == null) {
            return null;
        }

        if (superClassMap.containsKey(currentClassName)) {
            return superClassMap.get(currentClassName);
        }

        return null;

    }
}
