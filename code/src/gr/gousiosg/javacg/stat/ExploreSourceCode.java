package gr.gousiosg.javacg.stat;

import cn.edu.fudan.se.util.InvokeUtils4;

public class ExploreSourceCode {

    public static void main(String[] args) {
        String jarName = "apacheds-protocol-kerberos-2.0.0-M20.jar";
        String jarPath = "D:/wangying/" + jarName;
        InvokeUtils4.getJarInvokeMethodList(jarPath, jarName);
    }

}
