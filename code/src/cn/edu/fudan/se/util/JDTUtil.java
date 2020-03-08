package cn.edu.fudan.se.util;

import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.List;

public class JDTUtil {

    private static final String DECOMPILE_DIR = "H:/shibowen/decompile/";

    public static boolean checkMethodExist(String jarName, String methodName, String libDir) {
        //方法名
        String realMethodName = JavaMethodUtil.getMethodName(methodName);
        //参数列表
        List<String> params = JavaMethodUtil.getParamNames(methodName);

        String decompileOutPath = JavaMethodUtil.decompileJar(libDir + "/" + jarName, DECOMPILE_DIR);
        if (decompileOutPath == null) {
            //jar包不存在
            return false;
        }
        /**
         *  @param method org.apache.log4j.NDC$sdf.clear() or org.apache.log4j.NDC.clear()
         *  @return class org.apache.log4j.NDC$sdf or org.apache.log4j.NDC
         */
        //首先根据方法名获取其java文件的路径 javaPath
        String javaFile = JavaMethodUtil.getMethodClass(methodName);
        String javaPath = decompileOutPath + "/" + javaFile.replaceAll("\\.", "/") + ".java";

        if (!new File(javaPath).exists()) {
            //该方法所在的类在newVersion中不存在
            return false;
        } else {
            //存在
            CompilationUnit compilationUnit = JavaMethodUtil.getCompilationUnit(javaPath);
            TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
            List<BodyDeclaration> bodyDeclarationList = typeDeclaration.bodyDeclarations();

            return checkMethodExist(bodyDeclarationList, realMethodName, params);


        }
    }

    /**
     * @param bodyDeclarationList
     * @param methodName
     * @param params
     * @param type
     * @return
     */
    public static BodyDeclaration getMethod(List<BodyDeclaration> bodyDeclarationList, String
            methodName, List<String> params, int type) {

        for (BodyDeclaration bodyDeclaration : bodyDeclarationList) {
            if (type == JavaMethodUtil.VARIABLE_TYPE) {
                //变量
                if (bodyDeclaration instanceof FieldDeclaration) {
                    String identifierName = ((VariableDeclarationFragment) ((FieldDeclaration) bodyDeclaration).fragments().get(0)).getName().toString();
                    if (methodName.equals(identifierName)) {
                        return bodyDeclaration;
                    }
                } else {
                    continue;
                }
            } else {
                //方法
                if (bodyDeclaration instanceof MethodDeclaration) {
                    //先比较方法名
                    String methodDeclarationName = ((MethodDeclaration) bodyDeclaration).getName().getIdentifier();
                    List methodDeclarationParams = ((MethodDeclaration) bodyDeclaration).parameters();

                    if (!methodDeclarationName.equals(methodName)) {
                        continue;
                    }

                    //再比较参数列表
                    //1.参数列表先比较参数数量
                    int paramsSize = params.size();
                    int methodDeclarationParamsSize = methodDeclarationParams.size();
                    if (paramsSize != methodDeclarationParamsSize) {
                        continue;
                    }
                    //2.比较每一个参数
                    boolean paramSameFlag = true;
                    for (int index = 0; index < paramsSize; index++) {
                        //param:com.aaa.bbb.C
                        String param = params.get(index);
                        if (param.endsWith("...")) {
                            param = param.replaceAll("...", "[]");
                        }
                        if (param.contains("$")) {
                            param = param.replaceAll("\\$", ".");
                        }
                        if (param.contains("<")) {
                            param = param.substring(0, param.indexOf("<"));
                        }

                        //methodDeclarationParam: C or com.aaa.bbb.C
                        String methodDeclarationParam1 = ((SingleVariableDeclaration) methodDeclarationParams.get(index)).getType().toString();
                        String methodDeclarationParam = ((SingleVariableDeclaration) methodDeclarationParams.get(index)).toString();
                        String[] methodDeclarationParamSplit = methodDeclarationParam.split(" ");
                        String methodDeclarationParam2 = "";
                        if (methodDeclarationParamSplit.length == 2) {
                            methodDeclarationParam2 = methodDeclarationParamSplit[0];
                        } else {
                            methodDeclarationParam2 = methodDeclarationParamSplit[1];
                        }
                        methodDeclarationParam1 = formatParam(methodDeclarationParam1, methodDeclarationParam);
                        methodDeclarationParam2 = formatParam(methodDeclarationParam2, methodDeclarationParam);


                        if (param.equals(methodDeclarationParam1) || param.equals(methodDeclarationParam1) ||
                                param.endsWith(methodDeclarationParam1) || param.endsWith(methodDeclarationParam2) ||
                                param.equals("java.lang.Object") ||
                                methodDeclarationParam1.equals("Object") || methodDeclarationParam2.equals("Object")) {
                            continue;
                        } else {
                            //如果参数不相同，直接跳出循环
                            paramSameFlag = false;
                            break;
                        }
                    }
                    if (paramSameFlag) {
                        //参数完全相同
                        return bodyDeclaration;
                    } else {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    private static String formatParam(String methodDeclarationParam, String originParam) {
        if (methodDeclarationParam.contains("<")) {
            methodDeclarationParam = methodDeclarationParam.substring(0, methodDeclarationParam.indexOf("<"));
        }
        if (originParam.contains("...")) {
            methodDeclarationParam = methodDeclarationParam + "...";
        }
        if (methodDeclarationParam.endsWith("...")) {
            methodDeclarationParam = methodDeclarationParam.replace("...", "[]");
        }

        return methodDeclarationParam;
    }


    public static boolean checkMethodExist(List<BodyDeclaration> bodyDeclarationList, String
            methodName, List<String> params) {

        for (BodyDeclaration bodyDeclaration : bodyDeclarationList) {
            if (bodyDeclaration instanceof MethodDeclaration) {
                //先比较方法名
                String methodDeclarationName = ((MethodDeclaration) bodyDeclaration).getName().getIdentifier();
                List methodDeclarationParams = ((MethodDeclaration) bodyDeclaration).parameters();

                if (!methodDeclarationName.equals(methodName)) {
                    continue;
                }

                //再比较参数列表
                //1.参数列表先比较参数数量
                int paramsSize = params.size();
                int methodDeclarationParamsSize = methodDeclarationParams.size();
                if (paramsSize != methodDeclarationParamsSize) {
                    continue;
                }
                //2.比较每一个参数
                boolean paramSameFlag = true;
                for (int index = 0; index < paramsSize; index++) {
                    //param:com.aaa.bbb.C
                    String param = params.get(index);

                    //methodDeclarationParam: C or com.aaa.bbb.C
                    String methodDeclarationParam = ((SingleVariableDeclaration) methodDeclarationParams.get(index)).getType().toString();
                    if (param.equals(methodDeclarationParam) || param.endsWith(methodDeclarationParam)) {
                        continue;
                    } else {
                        //如果参数不相同，直接跳出循环
                        paramSameFlag = false;
                        break;
                    }
                }
                if (paramSameFlag) {
                    //参数完全相同
                    return true;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    public static BodyDeclaration getBody(List<BodyDeclaration> bodyDeclarationList, String
            methodName, List<String> params) {

        for (BodyDeclaration bodyDeclaration : bodyDeclarationList) {
            if (bodyDeclaration instanceof MethodDeclaration) {
                //先比较方法名
                String methodDeclarationName = ((MethodDeclaration) bodyDeclaration).getName().getIdentifier();
                List methodDeclarationParams = ((MethodDeclaration) bodyDeclaration).parameters();

                if (!methodDeclarationName.equals(methodName)) {
                    continue;
                }

                //再比较参数列表
                //1.参数列表先比较参数数量
                int paramsSize = params.size();
                int methodDeclarationParamsSize = methodDeclarationParams.size();
                if (paramsSize != methodDeclarationParamsSize) {
                    continue;
                }
                //2.比较每一个参数
                boolean paramSameFlag = true;
                for (int index = 0; index < paramsSize; index++) {
                    //param:com.aaa.bbb.C
                    String param = params.get(index);

                    //methodDeclarationParam: C or com.aaa.bbb.C
                    String methodDeclarationParam = ((SingleVariableDeclaration) methodDeclarationParams.get(index)).getType().toString();
                    if (param.equals(methodDeclarationParam) || param.endsWith(methodDeclarationParam)) {
                        continue;
                    } else {
                        //如果参数不相同，直接跳出循环
                        break;
                    }
                }
                if (paramSameFlag) {
                    //参数完全相同
                    return bodyDeclaration;
                } else {
                    continue;
                }
            }
        }
        return null;
    }


}
