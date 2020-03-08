package cn.edu.fudan.se.jardiff;

import cn.edu.fudan.se.preprocessingfile.DiffMetaInfo;
import cn.edu.fudan.se.util.DirUtil;
import cn.edu.fudan.se.util.JavaMethodUtil;
import cn.edu.fudan.se.util.JsonFileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.*;
import java.util.*;

public class JarDiff {

    public static void main(String args[]) {
        try {
            String outputDir = "C:/Users/huangkaifeng/Desktop/jardiff";
            String libPrev ="C:/Users/huangkaifeng/Desktop/jardiff/httpclient-4.0.2.jar";
            String libCurr = "C:/Users/huangkaifeng/Desktop/jardiff/httpclient-4.5.6.jar";
            File fPrev = new File(libPrev);
            File fCurr = new File(libCurr);
            String name = fPrev.getName().substring(0, fPrev.getName().length() - 4)+"__fdse__"+fCurr.getName().substring(0, fCurr.getName().length() - 4);
            JSONObject result = new JarDiff().doDiff(libPrev,libCurr ,outputDir);
            JsonFileUtil.save(outputDir+"/"+name+".json",result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param libPrevPath
     * @param libCurrPath
     * @return JSONObject result
     *
     * @throws FileNotFoundException
     */
    public JSONObject doDiff(String libPrevPath, String libCurrPath, String outputDir) throws FileNotFoundException {
        JSONObject returnObj = new JSONObject();
        String decomPrevPath = JavaMethodUtil.decompileJar(libPrevPath, outputDir);
        String decomCurrPath = JavaMethodUtil.decompileJar(libCurrPath, outputDir);
        List<File> decomPrevFiles = DirUtil.getAllJavaFilesOfADirectory(decomPrevPath);
        List<File> decomCurrFiles = DirUtil.getAllJavaFilesOfADirectory(decomCurrPath);
        Set<String> prevPaths = subPathOfFiles(decomPrevFiles, decomPrevPath.length());
        Set<String> currPaths = subPathOfFiles(decomCurrFiles, decomCurrPath.length());
        Set<String> result = new HashSet<>();
        result.addAll(prevPaths);
        result.retainAll(currPaths);
        JSONArray removedFiles = new JSONArray();
        JSONArray addedFiles = new JSONArray();
        JSONArray modifiedFiles = new JSONArray();
        for (String s : prevPaths) {
            if (!result.contains(s)) {
                JSONObject joo = DiffMetaInfo.singleFileMethodNameList(new FileInputStream(new File(decomPrevPath+s)),"method_list");
                joo.put("file_path",decomPrevPath + s);
                joo.put("file_sub_path",s);
                removedFiles.add(joo);
            }
        }
        for (String s : currPaths) {
            if (!result.contains(s)) {
                JSONObject joo = DiffMetaInfo.singleFileMethodNameList(new FileInputStream(new File(decomCurrPath+s)),"method_list");
                joo.put("file_path",decomCurrPath + s);
                joo.put("file_sub_path",s);
                addedFiles.add(joo);
            }
        }
        returnObj.put("REMOVED_FILES", removedFiles);
        returnObj.put("ADDED_FILES", addedFiles);
        for (String s : result) {
            try {
                File prevFile = new File(decomPrevPath + s);
                File currFile = new File(decomCurrPath + s);
                if (prevFile.length() != currFile.length()) {
                    InputStream prevIS = new FileInputStream(prevFile);
                    InputStream currIS = new FileInputStream(currFile);
                    System.out.println(decomPrevPath);
                    JSONObject joo = DiffMetaInfo.filePairDiffMethodNameList(prevIS,currIS);
                    joo.put("file_prev_path",decomPrevPath + s);
                    joo.put("file_curr_path",decomCurrPath + s);
                    joo.put("file_sub_path",s);
                    modifiedFiles.add(joo);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        returnObj.put("MODIFIED_FILES", modifiedFiles);
        return returnObj;

    }


    public static Map<DiffEntry.ChangeType,List<String>> diffMeta(String prevDecomDir, String currDecomDir) throws FileNotFoundException {
        List<File> decomPrevFiles = DirUtil.getAllJavaFilesOfADirectory(prevDecomDir);
        List<File> decomCurrFiles = DirUtil.getAllJavaFilesOfADirectory(currDecomDir);
        Set<String> prevPaths = subPathOfFiles(decomPrevFiles, prevDecomDir.length());
        Set<String> currPaths = subPathOfFiles(decomCurrFiles, currDecomDir.length());
        Set<String> result = new HashSet<>();
        result.addAll(prevPaths);
        result.retainAll(currPaths);
        Map<DiffEntry.ChangeType,List<String>> returnResult = new HashMap<>();
        List<String> add = new ArrayList<>();
        List<String> delete = new ArrayList<>();
        List<String> modify  = new ArrayList<>();
        for (String s : prevPaths) {
            if (!result.contains(s)) {
                delete.add(s);
            }
        }
        for (String s : currPaths) {
            if (!result.contains(s)) {
                add.add(s);
            }
        }
        returnResult.put(DiffEntry.ChangeType.ADD,add);
        returnResult.put(DiffEntry.ChangeType.DELETE,delete);
        for (String s : result) {
            File prevFile = new File(prevDecomDir + s);
            File currFile = new File(currDecomDir + s);
            if (prevFile.length() != currFile.length()) {
                modify.add(s);
            }
        }
        returnResult.put(DiffEntry.ChangeType.MODIFY,modify);
        return returnResult;
    }

    public static Set<String> subPathOfFiles(List<File> mList, int len) {
        Set<String> s = new HashSet<>();
        for (File f : mList) {
            s.add(f.getAbsolutePath().substring(len));
        }
        return s;
    }
}
