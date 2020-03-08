package cn.edu.fudan.se.util;

import cn.edu.fudan.se.effort.bean.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jdt.core.dom.BodyDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * jar包相关方法
 */
public class JarUtil {

    private static final String DECOMPILE_OUTPUT_PATH = "H:/shibowen/decompile/";

    static Map<String, List<String>> candidateMap;
    static Map<String, List<String>> projectDependencyMap;
    static Map<String, List<String>> dependencyMap;

    /**
     * 初始化
     */
    private static void init() {
        String content = FileUtil.read("input/all_candidates.txt");

        candidateMap = new Gson().fromJson(content, new TypeToken<Map<String, List<String>>>() {
        }.getType());
    }

    /**
     * 获取指定Jar包的候选Jar包列表
     *
     * @param artifactName
     * @param version
     * @return
     */
    public static List<String> getCandidateJarList(String artifactName, String version) {

        if (candidateMap == null) {
            init();
        }

        return JavaMethodUtil.getCandidates(candidateMap, artifactName, version);
    }

    /**
     * 读取某个库的三方依赖
     *
     * @return
     */
    public static List<String> getDependencyJarList(String projectId, String jarName) {

        if (projectDependencyMap == null) {
            String content = FileUtil.read("input/dependency.txt");
            projectDependencyMap = new Gson().fromJson(content, new TypeToken<Map<String, List<String>>>() {
            }.getType());
        }

        if (dependencyMap == null) {
            String content = FileUtil.read("input/dependency_map.txt");
            dependencyMap = new Gson().fromJson(content, new TypeToken<Map<String, List<String>>>() {
            }.getType());
        }

        Set<String> dependencySet = new HashSet<>();

        if (projectDependencyMap.containsKey(projectId)) {
            dependencySet.addAll(projectDependencyMap.get(projectId));
        }

        if (dependencyMap.containsKey(jarName)) {
            dependencySet.addAll(dependencyMap.get(jarName));
        }


        return new ArrayList<>(dependencySet);
    }

    /**
     * 比较两个不同版本Jar包的内容
     *
     * @param libDir
     * @param jarName
     * @param v1
     * @param v2
     */
    public static MethodLevelCompare compareJar(String libDir, String jarName, String v1, String v2) throws FileNotFoundException {

        File jar1 = new File(libDir + "/" + jarName + "-" + v1 + ".jar");
        File jar2 = new File(libDir + "/" + jarName + "-" + v2 + ".jar");

        if (!jar1.exists() || !jar2.exists()) {
            throw new FileNotFoundException();
        }

        String jar1DecompilePath = JavaMethodUtil.decompileJar(jar1.getAbsolutePath(), DECOMPILE_OUTPUT_PATH);
        String jar2DecompilePath = JavaMethodUtil.decompileJar(jar2.getAbsolutePath(), DECOMPILE_OUTPUT_PATH);

        JarFileResult jarFileResult = matchFilesIn2Jars(jar1DecompilePath, jar2DecompilePath);

        MethodLevelCompare methodLevelCompare = generateMethodDiff(libDir, jarName, v1, v2, jarFileResult);

        return methodLevelCompare;


    }

    /**
     * 根据jar包文件的生成结果生成不同种类文件下增、删、改、同的方法列表
     *
     * @param libDir
     * @param jarName
     * @param v1
     * @param v2
     * @param jarFileResult
     */
    private static MethodLevelCompare generateMethodDiff(String libDir, String jarName, String v1, String v2, JarFileResult jarFileResult) {

        List<String> addFileList = jarFileResult.getAddFileList();
        List<String> deleteFileList = jarFileResult.getDeleteFileList();
        List<String> sameList = jarFileResult.getUnionFileList();

        String addDecompilePath = jarName + "-" + v2 + "_" + "decompile";
        Map<String, ClassChangeInfo> addClassChangeMap = generateAddList(addDecompilePath, addFileList, "add");//增加的class文件列表


        String deleteDecompilePath = jarName + "-" + v1 + "_" + "decompile";
        Map<String, ClassChangeInfo> deleteClassChangeMap = generateAddList(deleteDecompilePath, deleteFileList, "delete");//删除的class文件列表

        String v1DecompilePath = deleteDecompilePath;
        String v2DecompilePath = addDecompilePath;

        Map<String, ClassChangeInfo>[] sameClassChangeMap = generateSameList(sameList, v1DecompilePath, v2DecompilePath);

        Map<String, ClassChangeInfo> deleteClassChangeMapInSameFile = sameClassChangeMap[0];
        Map<String, ClassChangeInfo> addClassChangeMapInSameFile = sameClassChangeMap[1];

        addClassChangeMap.putAll(addClassChangeMapInSameFile);
        deleteClassChangeMap.putAll(deleteClassChangeMapInSameFile);

        return new MethodLevelCompare(addClassChangeMap, deleteClassChangeMap);
    }

    /**
     * 比较相同文件内的方法
     *
     * @param sameList
     * @param v1DecompilePath
     * @param v2DecompilePath
     */
    private static Map<String, ClassChangeInfo>[] generateSameList(List<String> sameList, String v1DecompilePath, String v2DecompilePath) {

        int size = sameList.size();
        int index = 0;

        Map<String, ClassChangeInfo> deleteClassChangeMap = new HashMap<>();
        Map<String, ClassChangeInfo> addClassChangeMap = new HashMap<>();

        Map<String, ClassChangeInfo>[] changeMaps = new HashMap[2];

        for (String sameFile : sameList) {

            System.out.println("==same==" + String.valueOf(index) + "/" + String.valueOf(size));
            index++;

            if (sameFile.endsWith(".java")) {
                List<BodyDeclaration> v1BodyDeclarationList = JavaMethodUtil.getAllMethodInFile(DECOMPILE_OUTPUT_PATH + "/" + v1DecompilePath + "/" + sameFile);
                List<BodyDeclaration> v2BodyDeclarationList = JavaMethodUtil.getAllMethodInFile(DECOMPILE_OUTPUT_PATH + "/" + v2DecompilePath + "/" + sameFile);

                ClassChangeInfo[] classChangeInfos = BodyDeclarationUtil.compareBodyDeclarationList(v1BodyDeclarationList, v2BodyDeclarationList, sameFile);

                ClassChangeInfo deleteClassChangeInfo = classChangeInfos[0];
                ClassChangeInfo addClassChangeInfo = classChangeInfos[1];

                deleteClassChangeMap.put(sameFile, deleteClassChangeInfo);
                addClassChangeMap.put(sameFile, addClassChangeInfo);
            }
        }
        changeMaps[0] = deleteClassChangeMap;
        changeMaps[1] = addClassChangeMap;

        return changeMaps;
    }

    /**
     * 由新增文件列表生成新增方法列表
     *
     * @param decompilePath
     * @param addFileList
     * @return
     */
    private static Map<String, ClassChangeInfo> generateAddList(String decompilePath, List<String> addFileList, String tag) {

        int size = addFileList.size();
        int index = 0;

        Map<String, ClassChangeInfo> classChangeInfoMap = new HashMap<>();
        ClassChangeInfo classChangeInfo;
        for (String addFile : addFileList) {
            System.out.println("==" + tag + "==:" + String.valueOf(index) + "/" + String.valueOf(size));
            index++;
            if (addFile.endsWith(".java")) {
                List<BodyDeclaration> bodyDeclarationList = JavaMethodUtil.getAllMethodInFile(DECOMPILE_OUTPUT_PATH + "/" + decompilePath + "/" + addFile);

                classChangeInfo = BodyDeclarationUtil.formatBodyDeclaration(bodyDeclarationList, addFile);

                classChangeInfoMap.put(addFile, classChangeInfo);
            }
        }
        return classChangeInfoMap;
    }

    /**
     * 比较两个版本jar包的文件
     *
     * @param jar1DecompilePath
     * @param jar2DecompilePath
     */
    private static JarFileResult matchFilesIn2Jars(String jar1DecompilePath, String jar2DecompilePath) {

        List<String> jar1DecompileFileList = getDecompileFileList(jar1DecompilePath);
        List<String> jar2DecompileFileList = getDecompileFileList(jar2DecompilePath);

        JarFileResult jarFileResult = matchFilesIn2Jars(jar1DecompileFileList, jar2DecompileFileList);

        return jarFileResult;

    }

    /**
     * mapping 两个jar包中的文件
     *
     * @param jar1DecompileFileList
     * @param jar2DecompileFileList
     * @return
     */
    private static JarFileResult matchFilesIn2Jars(List<String> jar1DecompileFileList, List<String> jar2DecompileFileList) {

        List<String> unionList = new ArrayList<>(jar1DecompileFileList);
        //路径文件名完全相同
        unionList.retainAll(jar2DecompileFileList);

        List<String> uniqueJar1FileList = new ArrayList<>(jar1DecompileFileList);
        //删
        uniqueJar1FileList.removeAll(unionList);
        List<String> uniqueJar2FileList = new ArrayList<>(jar2DecompileFileList);
        //增
        uniqueJar2FileList.removeAll(unionList);

        return new JarFileResult(uniqueJar1FileList, uniqueJar2FileList, unionList);
    }


    /**
     * @param jarDecompilePath
     * @return
     */
    private static List<String> getDecompileFileList(String jarDecompilePath) {
        File jarDecompileFile = new File(jarDecompilePath);

        List<String> fileList = getAllDecompileFiles(jarDecompileFile);

        return fileList;
    }

    /**
     * 获取所有反编译文件的文件
     *
     * @param jarDecompileFile
     */
    private static List<String> getAllDecompileFiles(File jarDecompileFile) {

        List<String> fileList = new ArrayList<>();
        String jarDecompileFilePath = jarDecompileFile.getAbsolutePath().replaceAll("\\\\", "/");
        ;

        Queue<File> queue = new LinkedList<>();

        queue.add(jarDecompileFile);

        while (!queue.isEmpty()) {
            File file = queue.remove();

            if (file.isFile()) {

                String path = file.getAbsolutePath().replaceAll("\\\\", "/");

                String[] fileNames = path.split(jarDecompileFilePath);

                String fileName = fileNames[1];
                fileList.add(fileName);
                continue;
            }
            if (file.isDirectory()) {
                if (file.getName().equals("META-INF")) {
                    continue;
                }

                File[] files = file.listFiles();
                for (File file1 : files) {
                    queue.add(file1);
                }
            }
        }
        return fileList;
    }

}
