package cn.edu.fudan.se.preprocessingfile;

import cn.edu.fudan.se.preprocessingfile.data.BodyDeclarationPair;
import cn.edu.fudan.se.preprocessingfile.data.PreprocessedData;
import cn.edu.fudan.se.preprocessingfile.data.PreprocessedTempData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.Map.Entry;

//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

/**
 * 两个文件 预处理
 * 删除一摸一样的方法
 * 删除一摸一样的field
 * 删除一摸一样的内部类
 * 删除add method
 * 删除remove method
 * 删除内部类中的add / remove method
 * 保留 remove field 和add field 因为需要识别是否是refactor
 *
 * prefx 为 method field等所属的class，如果是内部类A, 那么prfix写到X.X.X.A.为止
 */
public class FilePairPreDiff {


    public FilePairPreDiff() {
        preprocessedData = new PreprocessedData();
        preprocessedTempData = new PreprocessedTempData();
        queue = new LinkedList<>();
    }

    private PreprocessedData preprocessedData;
    private PreprocessedTempData preprocessedTempData;

    class SrcDstPair{
        TypeDeclaration tpSrc;
        TypeDeclaration tpDst;
    }
    private Queue<SrcDstPair> queue;

    public void initFilePath(String prevPath, String currPath){
        preprocessedData.srcCu = JDTParserFactory.getCompilationUnit(prevPath);
        preprocessedData.dstCu = JDTParserFactory.getCompilationUnit(currPath);
        preprocessedData.loadTwoCompilationUnits(preprocessedData.srcCu, preprocessedData.dstCu, prevPath, currPath);
    }
    public void initFileContent(byte[] prevContent,byte[] currContent){
        try {
            preprocessedData.srcCu = JDTParserFactory.getCompilationUnit(prevContent);
            preprocessedData.dstCu = JDTParserFactory.getCompilationUnit(currContent);
            preprocessedData.loadTwoCompilationUnits(preprocessedData.srcCu, preprocessedData.dstCu, prevContent, currContent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public int compareTwoFile() {
        CompilationUnit cuSrc = preprocessedData.srcCu;
        CompilationUnit cuDst = preprocessedData.dstCu;
        preprocessedData.packageName = cuSrc.getPackage().getName().toString();
        preprocessedTempData.removeAllSrcComments(cuSrc, preprocessedData.srcLines);
        preprocessedTempData.removeAllDstComments(cuDst, preprocessedData.dstLines);
        if(cuSrc.types().size() != cuDst.types().size()){
            return -1;
        }
        for(int i = 0;i<cuSrc.types().size();i++){
            BodyDeclaration bodyDeclarationSrc = (BodyDeclaration) cuSrc.types().get(i);
            BodyDeclaration bodyDeclarationDst = (BodyDeclaration) cuDst.types().get(i);
            if ((bodyDeclarationSrc instanceof TypeDeclaration) && (bodyDeclarationDst instanceof TypeDeclaration)) {
                SrcDstPair srcDstPair = new SrcDstPair();
                srcDstPair.tpSrc = (TypeDeclaration) bodyDeclarationSrc;
                srcDstPair.tpDst = (TypeDeclaration) bodyDeclarationDst;
                this.queue.offer(srcDstPair);
            }else{
                return -1;
            }
        }
        while(queue.size()!=0){
            SrcDstPair tmp = queue.poll();
            compare(cuSrc,cuDst,tmp.tpSrc,tmp.tpDst);
        }
        return 0;
    }
    public void addSuperClass(TypeDeclaration type,List<String> list){
        List<Type> aa  = type.superInterfaceTypes();
        List<ASTNode> modifiers = type.modifiers();
        for(ASTNode node:modifiers){
            if(node instanceof Modifier){
                Modifier modifier = (Modifier)node;
                if(modifier.toString().equals("abstract")){
                    list.add("abstract---"+type.getName().toString());
                }
            }
        }
        if(aa!=null) {
            for (Type aaa : aa) {
                list.add("interface---"+aaa.toString());
            }
        }

        if(type.getSuperclassType()!=null) {
            list.add("superclass---"+type.getSuperclassType().toString());
        }
    }

    private void compare(CompilationUnit cuSrc,CompilationUnit cuDst,TypeDeclaration tdSrc,TypeDeclaration tdDst){
        TypeNodesTraversal astTraversal = new TypeNodesTraversal();
        addSuperClass(tdSrc,preprocessedData.getInterfacesAndFathers());
        addSuperClass(tdDst,preprocessedData.getInterfacesAndFathers());

        astTraversal.traverseSrcTypeDeclarationInit(preprocessedData, preprocessedTempData, tdSrc, tdSrc.getName().toString() + ".");
        // dst不同的method
        astTraversal.traverseDstTypeDeclarationCompareSrc(preprocessedData, preprocessedTempData, tdDst, tdDst.getName().toString() + ".");
        // 考虑后面的识别 method name变化，这里把remove的注释掉
        iterateVisitingMap();
        undeleteSignatureChange();
        preprocessedTempData.removeSrcRemovalList(cuSrc, preprocessedData.srcLines);
        preprocessedTempData.removeDstRemovalList(cuDst, preprocessedData.dstLines);
        // src 不同的method
        iterateVisitingMap2LoadContainerMap();
//        astTraversal.traverseSrcTypeDeclaration2Keys(preprocessedData,preprocessedTempData,tdSrc,tdSrc.getName().toString() + ".");

    }


    private void iterateVisitingMap() {
        for (Entry<BodyDeclarationPair, Integer> item : preprocessedTempData.srcNodeVisitingMap.entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            BodyDeclaration bd = bdp.getBodyDeclaration();
            if (bd instanceof TypeDeclaration) {
                switch (value) {
//                    case PreprocessedTempData.BODY_DIFFERENT_RETAIN:
//                    case PreprocessedTempData.BODY_FATHERNODE_REMOVE:
//                        break;
                    case PreprocessedTempData.BODY_INITIALIZED_VALUE:
                        this.preprocessedData.addBodiesDeleted(bdp);
                        this.preprocessedTempData.addToSrcRemoveList(bd);
                        TypeNodesTraversal.traverseTypeDeclarationSetVisited(preprocessedTempData, (TypeDeclaration) bd, bdp.getLocationClassString());
                        break;
                    case PreprocessedTempData.BODY_SAME_REMOVE:
                        this.preprocessedTempData.addToSrcRemoveList(bd);
                        TypeNodesTraversal.traverseTypeDeclarationSetVisited(preprocessedTempData, (TypeDeclaration) bd, bdp.getLocationClassString());
                        break;
                }
            }
        }
        for (Entry<BodyDeclarationPair, Integer> item : preprocessedTempData.srcNodeVisitingMap.entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            BodyDeclaration bd = bdp.getBodyDeclaration();

            if (!(bd instanceof TypeDeclaration)) {
                switch (value) {
                    case PreprocessedTempData.BODY_DIFFERENT_RETAIN:
                    case PreprocessedTempData.BODY_FATHERNODE_REMOVE:
                        break;
                    case PreprocessedTempData.BODY_INITIALIZED_VALUE:
                        this.preprocessedData.addBodiesDeleted(bdp);
                        preprocessedTempData.addToSrcRemoveList(bd);
                        break;
                    case PreprocessedTempData.BODY_SAME_REMOVE:
                        preprocessedTempData.addToSrcRemoveList(bd);
                        break;
                }
            }
//            if(bd instanceof MethodDeclaration){
//                MethodDeclaration md = (MethodDeclaration) bd;
////                if(md.getName().toString().equals("create")){
////                    System.out.println("aa");
//                    break;
//                }
//            }
        }
    }

    private void iterateVisitingMap2LoadContainerMap() {
        for (Entry<BodyDeclarationPair, Integer> item : preprocessedTempData.srcNodeVisitingMap.entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            switch (value) {
                case PreprocessedTempData.BODY_DIFFERENT_RETAIN:
//                    this.preprocessedData.entityContainer.addKey(bdp);
                    if(bdp.getBodyDeclaration() instanceof MethodDeclaration) {
//                        MethodDeclaration methodDeclaration;
//                        System.out.println(bdp.getLocationClassString() +)
                    }
                    break;
                case PreprocessedTempData.BODY_FATHERNODE_REMOVE:
                    break;
                case PreprocessedTempData.BODY_INITIALIZED_VALUE:
//                    preprocessedData.wyMethodsDeleted.add(bdp);
                    break;
                case PreprocessedTempData.BODY_SAME_REMOVE:
                    break;
            }
        }
//        this.preprocessedData.entityContainer.sortKeys();

    }

    public PreprocessedData getPreprocessedData() {
        return preprocessedData;
    }

    public void undeleteSignatureChange() {
        List<BodyDeclarationPair> addTmp = new ArrayList<>();
        for (BodyDeclarationPair bdpAdd : preprocessedData.getmBodiesAdded()) {
            if (bdpAdd.getBodyDeclaration() instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) bdpAdd.getBodyDeclaration();
                String methodName = md.getName().toString();
                List<BodyDeclarationPair> bdpDeleteList = new ArrayList<>();
                for (BodyDeclarationPair bdpDelete : preprocessedData.getmBodiesDeleted()) {
                    if (bdpDelete.getBodyDeclaration() instanceof MethodDeclaration) {
                        MethodDeclaration md2 = (MethodDeclaration) bdpDelete.getBodyDeclaration();
                        String methodName2 = md2.getName().toString();
                        if (potentialMethodNameChange(methodName, methodName2)) {
                            bdpDeleteList.add(bdpDelete);
                        }
                    }
                }
                if (bdpDeleteList.size() > 0) {
                    //remove的时候可能会有hashcode相同但是一个是在内部类的情况，但是这种情况很少见，所以暂时先不考虑
                    preprocessedTempData.dstRemovalNodes.remove(bdpAdd.getBodyDeclaration());
                    addTmp.add(bdpAdd);
                    for (BodyDeclarationPair bdpTmp : bdpDeleteList) {
                        this.preprocessedTempData.srcRemovalNodes.remove(bdpTmp.getBodyDeclaration());
                        this.preprocessedData.getmBodiesDeleted().remove(bdpTmp);
                        //todo
//                        this.preprocessedData.entityContainer.addKey(bdpTmp);
                    }
                }
            }

        }
        for (BodyDeclarationPair tmp : addTmp) {
            this.preprocessedData.getmBodiesAdded().remove(tmp);
        }
    }

    public boolean potentialMethodNameChange(String name1, String name2) {
        if (name1.length() == 0) return false;
        String tmp;
        if (name1.length() > name2.length()) {
            tmp = name1;
            name1 = name2;
            name2 = tmp;
        }
        int i;
        for (i = 0; i < name1.length(); i++) {
            char ch1 = name1.charAt(i);
            char ch2 = name2.charAt(i);
            if (ch1 != ch2) {
                break;
            }
        }
        double ii = (i * 1.0) / name1.length();
        if (ii > 0.7) {
//            System.out.println("Potential:"+name1+" "+name2);
            return true;
        }
        return false;
    }

//    public static void main(String args[]){
//        CompilationUnit cu = JDTParserFactory.getCompilationUnit("C:\\Users\\huangkaifeng\\Desktop\\test.java");
////        ASTNode ast = cu.types();
//        TypeDeclaration td = (TypeDeclaration) cu.types().get(0);
//        List list = td.bodyDeclarations();
//        MethodDeclaration md = (MethodDeclaration) list.get(0);
//        FieldDeclaration fd = (FieldDeclaration) list.get(1);
//        MethodDeclaration md2 = (MethodDeclaration) list.get(2);
//        JSONObject jo1 = bodyDeclarationToString(new BodyDeclarationPair(md,"aaaaaaa"));
//        JSONObject jo2 = bodyDeclarationToString(new BodyDeclarationPair(fd,"aaaaaaa"));
//        JSONObject jo3 = bodyDeclarationToString(new BodyDeclarationPair(md2,"aaaaaaa"));
//
//        System.out.println(jo1.toString());
//        System.out.println(jo2.toString());
//        System.out.println("aa");
//
//    }


    public  static JSONObject bodyDeclarationToString(BodyDeclarationPair a,String packageName){
        String prefix = a.getLocationClassString();
        BodyDeclaration bodyd = a.getBodyDeclaration();
        JSONObject jo = new JSONObject();
        if(bodyd instanceof MethodDeclaration){
            MethodDeclaration md = (MethodDeclaration)bodyd;
            jo.put("prefix",prefix);
            jo.put("method_name",md.getName().toString());
            jo.put("package",packageName);
            List mList = md.modifiers();
            Iterator iter = mList.iterator();
            JSONArray jArr = new JSONArray();
            while (iter.hasNext()){
                ASTNode node = (ASTNode) iter.next();
                if(node instanceof Modifier) {
                    Modifier mod = (Modifier) node;
                    jArr.add(mod.getKeyword().toString());
                }
            }
            jo.put("modifiers",jArr);
            List pList = md.parameters();
            Iterator iter2 = pList.iterator();
            JSONArray paramsList = new JSONArray();
            while (iter2.hasNext()){
                SingleVariableDeclaration var = (SingleVariableDeclaration) iter2.next();
                paramsList.add(var.toString());

            }
            jo.put("params",paramsList);
            if(md.getReturnType2()!=null) {
                jo.put("return", md.getReturnType2().toString());
            }
        }else if(bodyd instanceof FieldDeclaration){
            FieldDeclaration fd = (FieldDeclaration) bodyd;
            jo.put("prefix",prefix);
            jo.put("package",packageName);
            List l = fd.fragments();
            Iterator iter = l.iterator();
            JSONArray fieldList = new JSONArray();
            while(iter.hasNext()){
                VariableDeclarationFragment var = (VariableDeclarationFragment) iter.next();
                fieldList.add(var.toString());
            }
            jo.put("field_name",fieldList);
            jo.put("field",fd.toString().trim());
        }
        return jo;
    }

    public JSONObject methodsToJson(){
        List<BodyDeclarationPair> changedMethods = this.preprocessedData.wyMethodsChanged;
        List<BodyDeclarationPair> addedMethods = this.preprocessedData.getmBodiesAdded();
        List<BodyDeclarationPair> deletedMethods = this.preprocessedData.getmBodiesDeleted();
        JSONObject result = new JSONObject();
        JSONArray changeArr = new JSONArray();
        JSONArray addedArr = new JSONArray();
        JSONArray deletedArr = new JSONArray();
        result.put("changed_methods",changeArr);
        result.put("added_methods",addedArr);
        result.put("deleted_methods",deletedArr);
        String packageName = this.preprocessedData.packageName;
        for(BodyDeclarationPair a:changedMethods){
            changeArr.add(bodyDeclarationToString(a,packageName));
        }
        for(BodyDeclarationPair a:addedMethods){
            addedArr.add(bodyDeclarationToString(a,packageName));
        }
        for(BodyDeclarationPair a:deletedMethods){
            deletedArr.add(bodyDeclarationToString(a,packageName));
        }
        return result;
    }


}
