package cn.edu.fudan.se.effort.bean;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodInJarResult {

    public static final String JAR_NOT_FOUND = "jar not found";//jar包不存在
    public static final String CLASS_NOT_FOUND = "class not found";//类不存在
    public static final String METHOD_NOT_FOUND = "method not found";//方法不存在
    public static final String METHOD_FROM_JDK = "jdk method";//jdk的方法
    public static final String FOUND = "found";//存在


    private String result;
    private transient BodyDeclaration bodyDeclaration;
    private String bodyString;


    public MethodInJarResult(String result, BodyDeclaration bodyDeclaration, String bodyString) {
        this.result = result;
        this.bodyDeclaration = bodyDeclaration;
        this.bodyString = bodyString;
    }

    public MethodInJarResult(String result, BodyDeclaration bodyDeclaration) {
        this.result = result;
        this.bodyDeclaration = bodyDeclaration;
    }

    public String getBodyString() {
        return bodyString;
    }

    public void setBodyString(String bodyString) {
        this.bodyString = bodyString;
    }

    public BodyDeclaration getBodyDeclaration() {
        return bodyDeclaration;
    }

    public void setBodyDeclaration(BodyDeclaration bodyDeclaration) {
        this.bodyDeclaration = bodyDeclaration;
    }

    public MethodInJarResult() {
    }

    public String getResult() {

        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 初始化bodyString
     * <p>
     * //方法没有找到string为null
     * 找到但没有方法体 string为空字符串
     * 找到且有方法体 string正常
     */
    public void initBodyString() {

        if (bodyDeclaration != null) {
            //  Block body = bodyDeclaration
            if (bodyDeclaration instanceof MethodDeclaration) {
                Block body = ((MethodDeclaration) bodyDeclaration).getBody();

                bodyString = body == null ? "" : body.toString();
            } else if (bodyDeclaration instanceof FieldDeclaration) {
                bodyString = bodyDeclaration.toString();
            }

        } else {
            bodyString = null;
        }
    }
}
