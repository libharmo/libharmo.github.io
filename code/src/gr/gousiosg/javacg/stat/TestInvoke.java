package gr.gousiosg.javacg.stat;

import cn.edu.fudan.se.util.InvokeUtils4;

import java.util.List;
import java.util.Map;

public class TestInvoke {
    public static void main(String[] args) throws Exception {
        Map<String, List<String>> map = InvokeUtils4.getInvokeMethodByMethodName("TestJar22.jar", "d:/cs/jar/jar/TestJar22.jar", "java.lang.System.arraycopy(java.lang.Object,int,java.lang.Object,int,int)");
        int a = 1;
    }
}
