package cn.edu.fudan.se.effort;

import cn.edu.fudan.se.effort.callgraph.RecommendUtils;
import cn.edu.fudan.se.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendMain {

    public static final String DELETE_DIR = "H:\\shibowen\\callgraph\\delete_method_list/";
    public static final String OUTPUT_DIR = "H:/shibowen/recommend/";

    public static void main(String[] args) throws FileNotFoundException {
        File[] files = new File(DELETE_DIR).listFiles();
        Map<String, Map<String, Map<String, Object>>> resultMap;

        for (File file : files) {
            System.out.println(file);



            resultMap = new HashMap<>();
            String content = FileUtil.read(file.getAbsolutePath());
            Map<String, Map<String, List<String>>> apiMap = new Gson().fromJson(content, new TypeToken<Map<String, Map<String, List<String>>>>() {
            }.getType());

            for (Map.Entry<String, Map<String, List<String>>> artifactIdEntry : apiMap.entrySet()) {
                String artifactId = artifactIdEntry.getKey();

                resultMap.put(artifactId, new HashMap<>());

                Map<String, List<String>> versionMap = artifactIdEntry.getValue();
                for (Map.Entry<String, List<String>> apiListMapEntry : versionMap.entrySet()) {
                    String version = apiListMapEntry.getKey();
                    String originVersion = version.split("__fdse__")[0];
                    String recommendVersion = version.split("__fdse__")[1];
                    List<String> apiList = apiListMapEntry.getValue();

                    resultMap.get(artifactId).put(version, new HashMap<>());

                    for (String api : apiList) {
                        Object recommendObj = RecommendUtils.recommendMethod(artifactId, api, originVersion, recommendVersion);
                        if (recommendObj instanceof Map) {
                            //来自于javaDoc的推荐
                            String recommendDocString = new Gson().toJson(recommendObj);
                            resultMap.get(artifactId).get(version).put(api, recommendObj);

                        } else if (recommendObj instanceof Serializable) {
                            //来自于代码比较的推荐
                            String recommendDocString = new Gson().toJson(recommendObj);
                            resultMap.get(artifactId).get(version).put(api, recommendObj);
                        }

                    }

                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtil.writeFlie(OUTPUT_DIR + file.getName(), gson.toJson(resultMap));

        }
    }

}
