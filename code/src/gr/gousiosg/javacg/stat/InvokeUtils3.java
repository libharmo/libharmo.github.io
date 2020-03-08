package gr.gousiosg.javacg.stat;

import cn.edu.fudan.se.util.FileUtil;
import com.google.gson.Gson;
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

public class InvokeUtils3 {
    static private String JAVA_CALLGRAPH_CACHE_DIR = "D:/wangying/cache/";
    static private Gson gson = new Gson();

    static {
        File file = new File(JAVA_CALLGRAPH_CACHE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 读取缓存
     *
     * @param jarName
     * @return
     */
    private static Map<String, List<String>> getCache(String jarName) {
        String jarCacheFilePath = JAVA_CALLGRAPH_CACHE_DIR + jarName + ".txt";
        File cacheFile = new File(jarCacheFilePath);
        if (!cacheFile.exists()) {
            return null;
        }

        String cache = FileUtil.read(cacheFile.getAbsolutePath());
        Map<String, List<String>> cacheMap = new HashMap<>();
        cacheMap = gson.fromJson(cache, cacheMap.getClass());
        return cacheMap;
    }

    /**
     * jar包所有的调用关系
     *
     * @param jarName
     * @param jarPath
     * @return
     * @throws Exception
     */
    private static Map<String, List<String>> generateInvokeMap(String jarName, String jarPath) throws Exception {
        Map<String, List<String>> invokeMap = getCache(jarName);
        if (invokeMap != null) {
            return invokeMap;
        }

        String[] invokeStringList = getJarInvokeMethodList(jarPath);
        if (invokeStringList == null) {
            throw new Exception("error invoke string list!");
        } else {
            invokeMap = generateJarInvokeListToMap(invokeStringList);
        }

        FileUtil.writeFlie(JAVA_CALLGRAPH_CACHE_DIR + jarName + ".txt", gson.toJson(invokeMap));

        return invokeMap;
    }

    /**
     * 得到指定方法的调用关系
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
        //最后结果
        Map<String, List<String>> resultInvokeMethodMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(methodName);

        while (!queue.isEmpty()) {
            String method = queue.poll();
            if (checkJarInvokeMethodMapContains(jarInvokeMethodMap, method)) {
                //if (jarInvokeMethodMap.containsKey(method)) {
                List<String> invokeMethodList = jarInvokeMethodMap.get(method);
                if (resultInvokeMethodMap.containsKey(method)) {
                    //已经加过，不用再处理
                } else {
                    //如果该方法还没加过，加入key-value，且把调用的方法加进queue
                    resultInvokeMethodMap.put(method, new ArrayList<>());
                    for (String invokeMethod : invokeMethodList) {
                        resultInvokeMethodMap.get(method).add(invokeMethod);
                        queue.offer(invokeMethod);
                    }
                }
            }
        }

        return resultInvokeMethodMap;
    }

    private static boolean checkJarInvokeMethodMapContains(Map<String, List<String>> jarInvokeMethodMap, String method) {
        if (jarInvokeMethodMap.containsKey(method)) {
            return true;
        }
        String s = method;
        int index2 = s.indexOf('(');
        String subS = s.substring(0, index2);
        int index = subS.lastIndexOf('.');
        String className = s.substring(0, index);
        String methodName = s.substring(index+1, index2);
        String params = s.substring(index2);
        //initClassNames(classNames, className);
        while (true) {
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex == -1) {
                return jarInvokeMethodMap.containsKey(method);
            }
            StringBuilder classNameSb = new StringBuilder(className);
            classNameSb.replace(dotIndex, dotIndex + 1, "$");
            className = classNameSb.toString();
            String totalMethodName = className + "." + methodName + params;
            if (jarInvokeMethodMap.containsKey(totalMethodName)) {
                return true;
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
     * @return
     */
    private static String[] getJarInvokeMethodList(String jarPath) {
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
            }

            try (JarFile jar = new JarFile(f)) {
                Stream<JarEntry> entries = enumerationAsStream(jar.entries());

                String methodCalls = entries.
                        flatMap(e -> {
                            if (e.isDirectory() || !e.getName().endsWith(".class"))
                                return (new ArrayList<String>()).stream();

                            ClassParser cp = new ClassParser(jarPath, e.getName());
                            return getClassVisitor.apply(cp).start().methodCalls().stream();
                        }).
                        map(s -> s + "\n").
                        reduce(new StringBuilder(),
                                StringBuilder::append,
                                StringBuilder::append).toString();

                if (methodCalls == null) {
                    return null;
                } else {
                    return methodCalls.split("\n");
                }
                /*BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
                log.write(methodCalls);
                log.close();*/
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

}
