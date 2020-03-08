package cn.edu.fudan.se.effort;

import cn.edu.fudan.se.effort.bean.MethodInJarResult;
import cn.edu.fudan.se.util.FileUtil;
import cn.edu.fudan.se.util.JavaMethodUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueDeleteMethodMain {

    public static final String INPUT_FILE = "C:\\cs\\pyspace\\Data/delete_method_map.json";

    //         lib        api          recommend    origin
    static Map<String, Map<String, Map<String, List<String>>>> inputMap;
    //         lib        api          recommend    origin
    static Map<String, Map<String, Map<String, List<List<String>>>>> resultMap = new HashMap<>();

    private static String JAR_DIR = "H:/wangying/lib_all/";

    public static void main(String[] args) throws Exception {

        String content = FileUtil.read(INPUT_FILE);
        inputMap = new Gson().fromJson(content, new TypeToken<Map<String, Map<String, Map<String, List<String>>>>>() {
        }.getType());

        for (Map.Entry<String, Map<String, Map<String, List<String>>>> libEntry : inputMap.entrySet()) {

            String lib = libEntry.getKey();
            System.out.println("=" + lib);
            Map<String, Map<String, List<String>>> apiMap = libEntry.getValue();

            for (Map.Entry<String, Map<String, List<String>>> apiEntry : apiMap.entrySet()) {

                String api = apiEntry.getKey();
                Map<String, List<String>> recommendMap = apiEntry.getValue();
                System.out.println("==" + api);
                for (Map.Entry<String, List<String>> recommendEntry : recommendMap.entrySet()) {
                    String recommendVersion = recommendEntry.getKey();
                    List<String> originVersionList = recommendEntry.getValue();

                    System.out.println("===" + recommendVersion);
                    List<List<String>> originVersionsList = new ArrayList<>();
                    if (originVersionList.size() == 1) {
                        //如果原始版本列表只有一个，则不用找，直接加入结果
                        originVersionsList.add(originVersionList);
                        addInMap(resultMap, lib, api, recommendVersion, originVersionsList);
                    } else {

                        Map<Integer, List<String>> hashMethodMap = new HashMap<>();

                        for (String originVersion : originVersionList) {
                            System.out.println("====" + originVersion);
                            int type = JavaMethodUtil.checkMethodType(api);
                            MethodInJarResult methodInJarResult = JavaMethodUtil.getMethodInJar(lib + "-" + originVersion + ".jar", JAR_DIR, api, type, "", true, false);

                            if (methodInJarResult == null) {
                                methodInJarResult.initBodyString();
                            }
                            int hashCode = methodInJarResult.getBodyString().hashCode();

                            if (!hashMethodMap.containsKey(hashCode)) {
                                hashMethodMap.put(hashCode, new ArrayList<>());
                            }
                            List<String> tempOriginVersionList = hashMethodMap.get(hashCode);
                            tempOriginVersionList.add(originVersion);
                            hashMethodMap.put(hashCode, tempOriginVersionList);
                        }
                        for (Map.Entry<Integer, List<String>> tempMethodMapEntry : hashMethodMap.entrySet()) {
                            List<String> tempOriginVersionList2 = tempMethodMapEntry.getValue();
                            originVersionsList.add(tempOriginVersionList2);
                        }
                        addInMap(resultMap, lib, api, recommendVersion, originVersionsList);
                    }
                }
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtil.writeFlie("uniqueDeleteMethodMap.json", gson.toJson(resultMap));

    }

    /**
     * 加入结果
     *
     * @param resultMap
     * @param lib
     * @param api
     * @param recommendVersion
     * @param originVersionsList
     */
    private static void addInMap(Map<String, Map<String, Map<String, List<List<String>>>>> resultMap, String lib, String api, String recommendVersion, List<List<String>> originVersionsList) {
        if (!resultMap.containsKey(lib)) {
            resultMap.put(lib, new HashMap<>());
        }
        if (!resultMap.get(lib).containsKey(api)) {
            resultMap.get(lib).put(api, new HashMap<>());
        }
        if (!resultMap.get(lib).get(api).containsKey(recommendVersion)) {
            resultMap.get(lib).get(api).put(recommendVersion, new ArrayList<>());
        }
        resultMap.get(lib).get(api).get(recommendVersion).addAll(originVersionsList);
    }
}
