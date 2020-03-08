package cn.edu.fudan.se.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;

public class ExecuteCmd {

    /**
     * RunShell.execToString("java -version")
     *
     * @param command
     * @return
     */
    public static String execToString(String command) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);
            DefaultExecutor exec = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            exec.setStreamHandler(streamHandler);
            exec.execute(commandline);

            return (outputStream.toString());
        } catch (ExecuteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RunShell.execToString("java -version")
     *
     * @param command
     * @return
     */
    public static String execCmd(String command) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);
            DefaultExecutor exec = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            exec.setStreamHandler(streamHandler);
            exec.execute(commandline);

            return (outputStream.toString());
        } catch (ExecuteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * checkout("E:/maven500/netty__fdse__netty","0f1a2ca5ae01edff1f5ef667122b43f581ef92f4");
     *
     * @param projPath
     * @param commitId
     * @return
     */
    public static boolean checkout(String projPath, String commitId) {
//		String cmdStr = "git -C " + gitPath + " checkout -f " + commitId;
        String cmdStr = "git --git-dir=" + projPath + "/.git --work-tree=" + projPath + " checkout -f " + commitId;
        System.out.println(cmdStr);
        boolean success = runCmd(cmdStr);
        return success;
    }


    public static boolean runCmd(String cmd) {
        boolean success = false;
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmd);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message;
            while ((message = br.readLine()) != null) {
                sb.append(message + "\n");
            }
            System.out.println(sb);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean execute(String cmd, String dir) {
        boolean success = false;
        Runtime run = Runtime.getRuntime();
        try {
            File f = new File(dir);
            Process process = run.exec(cmd, null, f);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message;
            while ((message = br.readLine()) != null) {
                sb.append(message + "\n");
            }
            System.out.println(sb);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static String executeWithResult(String cmd, String dir) {
        StringBuffer sb = new StringBuffer();
        boolean success = false;
        Runtime run = Runtime.getRuntime();
        try {
            File f = new File(dir);
            Process process = run.exec(cmd, null, f);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);

            String message;
            while ((message = br.readLine()) != null) {
                sb.append(message + "\n");
            }
            System.out.println(sb);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


}
