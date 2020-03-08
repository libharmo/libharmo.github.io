package cn.edu.fudan.se.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DirUtil {

    public static List<File> getAllFilesOfADirectory(String path){
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        return result;
    }


    public static List<File> getAllJavaFilesOfADirectory(String path){
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        List<File> collect = result.stream()
                .filter(a -> a.getAbsolutePath().endsWith(".java"))
                .collect(Collectors.toList());

        return collect;
    }


    private static void browse(File dir, List<File> mList){
        File[] files = dir.listFiles();
        for(File f:files){
            if(f.isDirectory()){
                browse(f,mList);
            }else{
                mList.add(f);
            }
        }
    }

    /**
     * 去除 .. .符号
     * @return
     */
    public static String trimPathSub(String path) {
        path = path.replace("\\", "/");
        String[] data = path.split("/");
        Set<Integer> a = new HashSet<>();
        for (int i = 0; i < data.length; i++) {
            String temp = data[i];
            if (i != 0 && "..".equals(temp) && (!data[i - 1].equals(".."))) {
                a.add(i - 1);
                a.add(i);
            }
            if (i != 0 && ".".equals(temp)) {
                a.add(i);
            }
        }
        String res = "";
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals("")) {
                continue;
            }
            if (a.contains(i)) {
                continue;
            } else {
                res += data[i];
                res += "/";
            }
        }
        if (path.endsWith("/")) {
            return res;
        }
        return res.substring(0, res.length()-1);
    }

    public static String trimPath(String path) {
        String path2 = path;
        String newPath = null;
        while(true) {
            newPath = trimPathSub(path2);
            if (!newPath.equals(path2)) {
                path2 = newPath;
            } else {
                break;
            }
        }
        return newPath;
    }

}
