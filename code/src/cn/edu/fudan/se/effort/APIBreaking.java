package cn.edu.fudan.se.effort;

//import apidiff.APIDiff;
//import apidiff.Change;
//import apidiff.Result;
//import apidiff.enums.Classifier;

import apidiff2.APIDiff2;
import apidiff2.Result;
import apidiff2.enums.Classifier;
import cn.edu.fudan.se.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class APIBreaking {

    public static final String INPUT_DIR_Path = "H:\\shibowen\\method_level_compare/";
    public static final String LIB_DIR = "H:/wangying/lib_all/";

    public static final String OUTPUT_DIR_Path = "H:/shibowen/api_breaking/";

    public static final List<String> NOT_EXIST_JAR_LIST = new ArrayList<>();


    public static void main(String args[]) {
//
//        APIDiff diff = new APIDiff("mockito/mockito", "https://github.com/mockito/mockito.git");
//        diff.setPath("C:/Users/huangkaifeng/Desktop");
//        Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
//        for(Change changeMethod : result.getChangeMethod()){
//            System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
//        }

       /* List<Change> fieldChanges = result.getChangeField();
        List<Change> methodChanges = result.getChangeMethod();
        List<Change> typeChanges = result.getChangeType();

        int index = 0;
        for (Change change : typeChanges) {
            System.out.println(String.valueOf(index) + "   " + change.getCategory().toString());
//            if (index==10){
//                System.out.println(new Gson().toJson(change));
//            }
            index++;
        }


        System.out.println("a");*/


        File inputDirFile = new File(INPUT_DIR_Path);
        File[] files = inputDirFile.listFiles();

        for (File file : files) {
            String fileName = file.getName();
            if (!file.getName().contains("elasticsearch")) {
                continue;
            }

            if (fileName.equals("elasticsearch__fdse__1.1.1__fdse__2.2.0.json")) {
                continue;
            }

            System.out.println(fileName);
            String artifactId = fileName.split(".json")[0].split("__fdse__")[0];
            String originVersion = fileName.split(".json")[0].split("__fdse__")[1];
            String recommendVersion = fileName.split(".json")[0].split("__fdse__")[2];

            System.out.println(artifactId + " " + originVersion + " " + recommendVersion);
            generateApiBreakingResult(LIB_DIR, artifactId, originVersion, recommendVersion);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtil.writeFlie("API_BREAKING_NOT_EXIST_JAR.json", gson.toJson(NOT_EXIST_JAR_LIST));

//        generateApiBreakingResult(LIB_DIR, "commons-io", "2.3", "2.6");

    }

    private static void generateApiBreakingResult(String libDir, String artifactId, String originVersion, String recommendVersion) {
        /*APIDiff2 diff = new APIDiff2("D:/wangying/test/commons-io-2.3.jar", "D:/wangying/test/commons-io-2.6.jar");
        diff.setPath("D:/wangying/test");*/


        String outputFilePath = OUTPUT_DIR_Path + "/" + artifactId + "__fdse__" + originVersion + "__fdse__" + recommendVersion + ".json";

        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            return;
        }


        String originJarPath = libDir + "/" + artifactId + "-" + originVersion + ".jar";
        String recommendJarPath = libDir + "/" + artifactId + "-" + recommendVersion + ".jar";

        File originJar = new File(originJarPath);
        File recommendJar = new File(recommendJarPath);

        if (originJar.exists() && recommendJar.exists()) {
            APIDiff2 diff = new APIDiff2(originJarPath, recommendJarPath);
            diff.setPath("H:/shibowen/decompile");
            diff.initJars();
            Result result = diff.detectChange(Classifier.API);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtil.writeFlie(outputFilePath, gson.toJson(result));
//            System.out.println(gson.toJson(result));
        } else {
            if (!originJar.exists()) {
                NOT_EXIST_JAR_LIST.add(originJarPath);
            }

            if (!recommendJar.exists()) {
                NOT_EXIST_JAR_LIST.add(recommendJarPath);
            }


        }
    }

}
