package cn.edu.fudan.se.preprocessingfile;

import cn.edu.fudan.se.preprocessingfile.data.BodyDeclarationPair;
import cn.edu.fudan.se.util.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DiffMetaInfo {

    public static JSONObject filePairDiffMethodNameList(InputStream prevIS, InputStream currIS) throws IOException {
        byte[] prev = FileUtil.toByteArray(prevIS);
        byte[] curr = FileUtil.toByteArray(currIS);
        FilePairPreDiff preDiff = new FilePairPreDiff();
        preDiff.initFileContent(prev, curr);
        int result = preDiff.compareTwoFile();
        JSONObject joo = preDiff.methodsToJson();
        return joo;
    }

    public static JSONObject singleFileMethodNameList(InputStream is, String key){
        try {
            JSONObject result = new JSONObject();
            JSONArray arr = new JSONArray();
            CompilationUnit currUnit = JDTParserFactory.getCompilationUnit(is);
            if(currUnit.types().size()== 0 ||
                    !(currUnit.types().get(0) instanceof TypeDeclaration)){
                result.put(key,arr);
                return result;
            }
            TypeDeclaration type = (TypeDeclaration) currUnit.types().get(0);
            String packageName = currUnit.getPackage().getName().toString();
            List<BodyDeclaration> bodyList = type.bodyDeclarations();
            for (BodyDeclaration bd : bodyList) {
                JSONObject joo = FilePairPreDiff.bodyDeclarationToString(new BodyDeclarationPair(bd,type.getName().toString()+"."),packageName);
                arr.add(joo);
            }
            result.put(key,arr);
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
