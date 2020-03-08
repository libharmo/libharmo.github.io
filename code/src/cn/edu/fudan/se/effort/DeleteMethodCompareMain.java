package cn.edu.fudan.se.effort;

import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JavaMethodUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteMethodCompareMain {

    public static final String CALL_GRAPH_DIR = "H:\\shibowen\\callgraph\\buggyCallgraph2/";
    public static final String VERSION_RECOMMEND_DIR = "H:\\shibowen\\callgraph\\delete_method/";
    //         filePath    jar        method    candidate     invokeMethod  info
    static Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> callGraphMap;
    static Map<String, List<String>> versionMap;

    public static void main(String[] args) {

        File callGraphDir = new File(CALL_GRAPH_DIR);
        File versionRecommendDir = new File(VERSION_RECOMMEND_DIR);

        File[] callGraphFiles = callGraphDir.listFiles();

        Map<String, Map<String, List<String>>> result;
        for (File file : callGraphFiles) {
            result = new HashMap<>();
            String fileName = file.getName();
            String versionFileName = VERSION_RECOMMEND_DIR + fileName;

            File versionRecommendFile = new File(versionFileName);
            if (!versionRecommendFile.exists()) {
                continue;
            }

            callGraphMap = new Gson().fromJson(FileUtil.read(file.getAbsolutePath()), new TypeToken<Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>() {
            }.getType());
            versionMap = new Gson().fromJson(FileUtil.read(versionRecommendFile.getAbsolutePath()), new TypeToken<Map<String, List<String>>>() {
            }.getType());

            for (Map.Entry<String, Map<String, Map<String, Map<String, Map<String, String>>>>> filePathEntry : callGraphMap.entrySet()) {
                String filePath = filePathEntry.getKey();
                Map<String, Map<String, Map<String, Map<String, String>>>> filePathMap = filePathEntry.getValue();

                for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> jarEntry : filePathMap.entrySet()) {
                    String jarName = jarEntry.getKey();
                    Map<String, Map<String, Map<String, String>>> jarMap = jarEntry.getValue();

                    for (Map.Entry<String, Map<String, Map<String, String>>> apiEntry : jarMap.entrySet()) {
                        String api = apiEntry.getKey();
                        Map<String, Map<String, String>> candidateMap = apiEntry.getValue();

                        List<String> recommendVersionList = getRecommendVersion(jarName, versionMap);

                        if (recommendVersionList == null) {
                            continue;
                        }

                        for (String recommendVersion : recommendVersionList) {
                            String tempJarName = jarName.split("\\.jar")[0];
                            String[] jarInfo = JavaMethodUtil.getJarInfo(tempJarName);
                            String artifactId = jarInfo[0];
                            String version = jarInfo[1];

                            String recommendJarName = artifactId + "-" + recommendVersion + ".jar";

                            if (candidateMap.containsKey(recommendJarName)) {

                                Map<String, String> invokeMap = candidateMap.get(recommendJarName);

                                if (invokeMap.size() == 0 || invokeMap == null) {
                                    //没有跑出callgraph，是删除的api

                                    addInMap(result, artifactId, version, recommendVersion, api);
                                } else {
                                    Object[] objects = specialContains(invokeMap, api);
                                    boolean same = (boolean) objects[0];
                                    if (same) {
                                        //如果能找到
                                        String flag = (String) objects[1];
                                        if (!flag.equals("same") && !flag.equals("modify")) {
                                            addInMap(result, artifactId, version, recommendVersion, api);
                                        }
                                    } else {
                                        //如果找不到
                                        addInMap(result, artifactId, version, recommendVersion, api);
                                    }

                                }
                            }

                        }

                    }

                }


            }

            System.out.println(fileName);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtil.writeFlie("H:\\shibowen\\callgraph\\delete_method_list/" + fileName, gson.toJson(result));
        }


    }

    private static void addInMap(Map<String, Map<String, List<String>>> result, String artifactId, String originVersion, String recommendVersion, String api) {
        if (!result.containsKey(artifactId)) {
            result.put(artifactId, new HashMap<>());
        }
        String versionKey = originVersion + "__fdse__" + recommendVersion;
        if (!result.get(artifactId).containsKey(versionKey)) {
            result.get(artifactId).put(versionKey, new ArrayList<>());
        }
        result.get(artifactId).get(versionKey).add(api);
    }

    private static Object[] specialContains(Map<String, String> invokeMap, String api) {

        Object[] objects = new Object[2];

        for (Map.Entry<String, String> invokeMapEntry : invokeMap.entrySet()) {
            String key = invokeMapEntry.getKey();
            if (specialEqual(key, api)) {
                objects[0] = true;
                objects[1] = invokeMapEntry.getValue();
                return objects;
            }
        }
        objects[0] = false;
        objects[1] = null;
        return objects;
    }

    private static boolean specialEqual(String key, String api) {

        String[] keys = key.split("[$.]");
        String[] apis = api.split("[$.]");

        return checkArrays(keys, apis);


    }

    private static boolean checkArrays(String[] keys, String[] apis) {
        if (keys.length != apis.length) {
            return false;
        }

        int length = keys.length;
        for (int i = 0; i < length; i++) {
            String key = keys[i];
            String api = apis[i];

            if (!key.equals(api)) {
                return false;
            }

        }
        return true;
    }

    private static List<String> getRecommendVersion(String jarName, Map<String, List<String>> versionMap) {

        List<String> recommendVersionList = new ArrayList<>();

        jarName = jarName.split("\\.jar")[0];
        String[] jarInfo = JavaMethodUtil.getJarInfo(jarName);

        String artifactId = jarInfo[0];
        String version = jarInfo[1];

        for (Map.Entry<String, List<String>> versionMapEntry : versionMap.entrySet()) {
            String recommendVersion = versionMapEntry.getKey();
            String[] recommendVersions = recommendVersion.split("__fdse__");
            String recommendArtifactId = recommendVersions[1];

            if (recommendArtifactId.equals(artifactId)) {
                recommendVersionList.addAll(versionMapEntry.getValue());
                break;
            }
        }

        if (recommendVersionList.size() == 0) {
            return null;
        }

        return recommendVersionList;
    }

}
