package cn.edu.fudan.se.util;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;

public class ZipUtil {

    public static void zip(String path, String newPath) {
        try {
            (new File(newPath)).mkdirs();
            File f = new File(path);
            if ((!f.exists()) && (f.length() <= 0)) {
                throw new RuntimeException("not find " + path + "!");
            }
            //一定要加上编码，之前解压另外一个文件，没有加上编码导致不能解压
            ZipFile zipFile = new ZipFile(f, "gbk");
            String gbkPath, strtemp;
            Enumeration<ZipEntry> e = zipFile.getEntries();
            while (e.hasMoreElements()) {
                org.apache.tools.zip.ZipEntry zipEnt = e.nextElement();
                gbkPath = zipEnt.getName();
                strtemp = newPath + File.separator + gbkPath;
                if (zipEnt.isDirectory()) { //目录
                    File dir = new File(strtemp);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    continue;
                } else {
                    // 读写文件
                    InputStream is = zipFile.getInputStream(zipEnt);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    // 建目录
                    String strsubdir = gbkPath;
                    for (int i = 0; i < strsubdir.length(); i++) {
                        if (strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {
                            String temp = newPath + File.separator
                                    + strsubdir.substring(0, i);
                            File subdir = new File(temp);
                            if (!subdir.exists())
                                subdir.mkdir();
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(strtemp);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int len;
                    byte[] buff = new byte[5120];
                    while ((len = bis.read(buff)) != -1) {
                        bos.write(buff, 0, len);
                    }
                    is.close();
                    bis.close();
                    bos.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
//            logger.error("解压文件出现异常：", e);
//            isUnZipSuccess = false;
//            System.out.println("extract file error: " + zipFilePath);
//            fileOperateUtil.WriteStringToFile(fileOperateUtil.logPath, "extract file error: " + zipFilePath);
            e.printStackTrace();
        }
    }

}
