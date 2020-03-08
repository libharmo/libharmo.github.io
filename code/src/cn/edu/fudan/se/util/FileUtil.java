package cn.edu.fudan.se.util;

import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;

import java.io.*;

public class FileUtil {
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static InputStream open(ObjectId blobId, Repository db) throws IOException, IncorrectObjectTypeException {
        if (blobId == null)
            return new ByteArrayInputStream(new byte[0]);

        try {
            WorkingTreeOptions workingTreeOptions = db.getConfig().get(WorkingTreeOptions.KEY);
            switch (workingTreeOptions.getAutoCRLF()) {
                case INPUT:
                    // When autocrlf == input the working tree could be either CRLF
                    // or LF, i.e. the comparison
                    // itself should ignore line endings.
                case FALSE:
                    return db.open(blobId, Constants.OBJ_BLOB).openStream();
                case TRUE:
                default:
                    return db.open(blobId, Constants.OBJ_BLOB).openStream();
//				return new AutoCRLFInputStream(db.open(blobId, Constants.OBJ_BLOB).openStream(), true);
            }
        } catch (MissingObjectException notFound) {
            return null;
        }
    }

    public static void appendLine(String path, String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter(path, true);
            writer.write(content + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void appendFile(String path, String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(path, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getAllPomFiles(String projectPath) {
        String[] INCLUDE_ALL_POMS = new String[]{"**\\pom.xml"};

        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(projectPath);
        scanner.setIncludes(INCLUDE_ALL_POMS);
        scanner.scan();
//        final List<String> poms = new ArrayList();
//		for (int ctr = 0; ctr < scanner.getIncludedFiles().length; ctr++) {
//			final File file = new File(projectPath, scanner.getIncludedFiles()[ctr]);
//			if (file.exists()) {
//				System.out.println(file.getAbsolutePath());
//				// poms.add(file);
//			}
//		}
        return scanner.getIncludedFiles();

//        return poms;
    }

    /**
     * 使用FileWriter类写文本文件
     */
    public static void writeFlie(String fileName, String content) {

        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读文件
     *
     * @param path
     * @return
     */
    public static String read(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            // System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));

            String tempString = "";
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                result.append(tempString).append("\r\n");
                line++;
            }
            result = new StringBuilder(result.substring(0, result.length() - 2));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    return result.toString();
                } catch (IOException e1) {
                }
            }
        }

        return null;
    }


    public static String unzipJar(String unzipPath, String jarPath, String jarName) {
        String newPath = unzipPath + jarName.substring(0, jarName.length() - 4) + "_unzip";
        File ff = new File(newPath);
        if (!ff.exists()) {
            ZipUtil.zip(jarPath, newPath);
        }
        return newPath;
    }


    /**
     * 将依赖的gId aId v转化为对应的jar包路径和名字
     *
     * @return
     */
    public static String[] dependency2JarPath(String mavenRepositoryPath, String groupId, String artifactId, String version) {
        //C:\Users\Basti031\.m2\repository\antlr\antlr\2.7.2
        String jarDir = mavenRepositoryPath + "/" + groupId + "/" + artifactId + "/" + version + "/";
        File jarDirFile = new File(jarDir);

        String[] jarInfo = new String[2];
        File[] files = jarDirFile.listFiles();
        if (files == null) {
            jarInfo[0] = "";
            jarInfo[1] = "";
            return jarInfo;
        }

        boolean find = false;
        for (File file : files) {
            if (file.getName().endsWith("jar")) {
                //找到jar包
                jarInfo[0] = file.getName();
                jarInfo[1] = file.getAbsolutePath();
                find = true;
                break;
            }
        }
        if (!find) {
            jarInfo[0] = "";
            jarInfo[1] = "";
        }
        return jarInfo;
    }


    /**
     * 检测指定路径的文件是否存在
     *
     * @param path
     */
    public static boolean checkFileExists(String path) {
        return new File(path).exists();
    }
}
