package apidiff2.internal.visitor;

import apidiff2.enums.Classifier;
import apidiff2.internal.analysis.comparator.ComparatorMethod;
import apidiff2.internal.service.git.GitFile;
import apidiff2.internal.util.UtilTools;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class APIVersion {
	
	private ArrayList<TypeDeclaration> apiAccessibleTypes = new ArrayList<TypeDeclaration>();
	private ArrayList<TypeDeclaration> apiNonAccessibleTypes = new ArrayList<TypeDeclaration>();
	private ArrayList<EnumDeclaration> apiAccessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<EnumDeclaration> apiNonAccessibleEnums = new ArrayList<EnumDeclaration>();
	private Map<ChangeType, List<GitFile>> mapModifications = new HashMap<ChangeType, List<GitFile>>();
	private List<String> modify = new ArrayList<>();
	private List<String> listFilesMofify = new ArrayList<String>();
	private Classifier classifierAPI;
	private String nameProject;
	
	private Logger logger = LoggerFactory.getLogger(APIVersion.class);
	
	private String path;

	public APIVersion(final String path, final List<String> listModifications, Classifier classifierAPI) {
		try {
			this.classifierAPI = classifierAPI;
			this.modify = listModifications;
			this.path = path;
//			this.nameProject = file.getAbsolutePath().replaceAll(this.path + "/", "");
//	    	String prefix = file.getAbsolutePath() + "/";
			for(String file : this.modify){
				this.parseFilesInDir(new File(path+"/"+file), false);
			}
		} catch (IOException e) {
			this.logger.error("Erro ao criar APIVersion", e);
		}
	}
	
	public APIVersion(final String nameProject, Classifier classifierAPI) {
		try {
			this.nameProject = nameProject;
			this.classifierAPI = classifierAPI;
			File path = new File(this.path + "/" + this.nameProject);
			this.parseFilesInDir(path, true);
		} catch (IOException e) {
			this.logger.error("Erro ao criar APIVersion", e);
		}
		
	}

	public void parseFilesInDir(File file, final Boolean ignoreTreeDiff) throws IOException {
		if (file.isFile()) {
			String simpleNameFile = UtilTools.getSimpleNameFileWithouPackageWithNameLibrary(this.path, file.getAbsolutePath(), this.nameProject);
			if (UtilTools.isJavaFile(file.getName()) &&
//					this.isFileModification(file, ignoreTreeDiff) &&
					UtilTools.isAPIByClassifier(simpleNameFile, this.classifierAPI)) {
				this.parse(UtilTools.readFileToString(file.getAbsolutePath()), file, ignoreTreeDiff);		
			}
		}
//		else {
//			if(file.listFiles() != null){
//				for (File f : file.listFiles()) {
//					this.parseFilesInDir(f, ignoreTreeDiff);
//				}
//			}
//		}
	}

	public void parse(String str, File source, final Boolean ignoreTreeDiff) throws IOException {
		
		if(this.mapModifications.size() > 0 && !this.isFileModification(source,ignoreTreeDiff)){
			return;
		}
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String[] classpath = java.lang.System.getProperty("java.class.path").split(";");
		String[] sources = { source.getParentFile().getAbsolutePath() };

		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		parser.setUnitName(source.getAbsolutePath());

		parser.setCompilerOptions(options);
//		parser.setEnvironment(null, sources, new String[] { "UTF-8" },	true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

//		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" },	true);
//		CompilationUnit compilationUnit = null;
		try {
			parser.setEnvironment(null, null, null,	true);
			CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
			apidiff2.internal.visitor.TypeDeclarationVisitor visitorType = new apidiff2.internal.visitor.TypeDeclarationVisitor();
			apidiff2.internal.visitor.EnumDeclarationVisitor visitorEnum = new apidiff2.internal.visitor.EnumDeclarationVisitor();

			compilationUnit.accept(visitorType);
			compilationUnit.accept(visitorEnum);

			this.configureAcessiblesAndNonAccessibleTypes(visitorType);
			this.configureAcessiblesAndNonAccessibleEnums(visitorEnum);
		} catch (Exception e) {
			this.logger.error("Erro ao criar AST sem source", e);
		}

	}

	private void configureAcessiblesAndNonAccessibleTypes(TypeDeclarationVisitor visitorType){
		this.apiNonAccessibleTypes.addAll(visitorType.getNonAcessibleTypes());
		this.apiAccessibleTypes.addAll(visitorType.getAcessibleTypes());
	}

	private void configureAcessiblesAndNonAccessibleEnums(EnumDeclarationVisitor visitorType){
		this.apiNonAccessibleEnums.addAll(visitorType.getNonAcessibleEnums());
		this.apiAccessibleEnums.addAll(visitorType.getAcessibleEnums());
	}

	private Boolean isFileModification(final File source, final Boolean ignoreTreeDiff){
		return (ignoreTreeDiff || this.listFilesMofify.contains(source.getAbsolutePath()))? true: false;
	}

	public ArrayList<EnumDeclaration> getApiAccessibleEnums() {
		return apiAccessibleEnums;
	}

	public ArrayList<EnumDeclaration> getApiNonAccessibleEnums() {
		return apiNonAccessibleEnums;
	}

	public ArrayList<TypeDeclaration> getApiAcessibleTypes(){
		return this.apiAccessibleTypes;
	}

	public ArrayList<TypeDeclaration> getApiNonAcessibleTypes(){
		return this.apiNonAccessibleTypes;
	}

	public ArrayList<AbstractTypeDeclaration> getTypesPublicAndProtected() {
		ArrayList<AbstractTypeDeclaration> list = new ArrayList<AbstractTypeDeclaration>();
		list.addAll(this.getApiAcessibleTypes());
		list.addAll(this.getApiAccessibleEnums());
		return list;
	}


	public ArrayList<AbstractTypeDeclaration> getTypesPrivateAndDefault() {
		ArrayList<AbstractTypeDeclaration> list = new ArrayList<AbstractTypeDeclaration>();
		list.addAll(this.getApiNonAcessibleTypes());
		list.addAll(this.getApiNonAccessibleEnums());
		return list;
	}

	public EnumDeclaration getVersionNonAccessibleEnum(EnumDeclaration enumVersrionReference){
		for (EnumDeclaration enumDeclarion : this.apiNonAccessibleEnums) {
			if(enumDeclarion.resolveBinding() != null && enumVersrionReference.resolveBinding() != null){
				if(enumDeclarion.resolveBinding().getQualifiedName().equals(enumVersrionReference.resolveBinding().getQualifiedName())){
					return enumDeclarion;
				}
			}
		}

		return null;
	}

	public EnumDeclaration getVersionAccessibleEnum(EnumDeclaration enumVersrionReference){
		for (EnumDeclaration enumDeclarion : this.apiAccessibleEnums) {
			if(enumDeclarion.resolveBinding() != null && enumVersrionReference.resolveBinding() != null){
				if(enumDeclarion.resolveBinding().getQualifiedName().equals(enumVersrionReference.resolveBinding().getQualifiedName())){
					return enumDeclarion;
				}
			}
		}

		return null;
	}

	public AbstractTypeDeclaration getVersionNonAccessibleType(AbstractTypeDeclaration typeVersrionReference){
		for (AbstractTypeDeclaration typeDeclarion : this.getTypesPrivateAndDefault()) {
			if(typeDeclarion.resolveBinding() != null && typeVersrionReference.resolveBinding() != null){
				if(typeDeclarion.resolveBinding().getQualifiedName().equals(typeVersrionReference.resolveBinding().getQualifiedName())){
					return typeDeclarion;
				}
			}
		}

		return null;
	}


	public AbstractTypeDeclaration getVersionAccessibleType(AbstractTypeDeclaration typeVersrionReference){
		for (AbstractTypeDeclaration typeDeclarion : this.getTypesPublicAndProtected()) {
			if(typeDeclarion.resolveBinding() != null && typeVersrionReference.resolveBinding() != null){
				if(typeDeclarion.resolveBinding().getQualifiedName().equals(typeVersrionReference.resolveBinding().getQualifiedName())){
					return typeDeclarion;
				}
			}
		}
		return null;
	}

	public boolean containsType(TypeDeclaration type){
		return this.containsAccessibleType(type) || this.containsNonAccessibleType(type);
	}

	public boolean containsAccessibleType(AbstractTypeDeclaration type){
		return this.getVersionAccessibleType(type) != null;
	}

	public boolean containsNonAccessibleType(AbstractTypeDeclaration type){
		return this.getVersionNonAccessibleType(type) != null;
	}

	public boolean containsAccessibleEnum(EnumDeclaration type){
		return this.getVersionAccessibleEnum(type) != null;
	}

	public boolean containsNonAccessibleEnum(EnumDeclaration type){
		return this.getVersionNonAccessibleEnum(type) != null;
	}

	public FieldDeclaration getVersionField(FieldDeclaration field, TypeDeclaration type){
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for (FieldDeclaration versionField : versionType.getFields()) {
					String name1 = UtilTools.getFieldName(versionField);
					String name2  = UtilTools.getFieldName(field);
					if(name1 != null && name2 != null && name1.equals(name2)){
						return versionField;
					}
				}
			}
		}
		return null;
	}

	public ArrayList<MethodDeclaration> getAllEqualMethodsByName(MethodDeclaration method, TypeDeclaration type) {
		ArrayList<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(versionMethod.getName().toString().equals(method.getName().toString()))
						result.add(versionMethod);
				}
			}
		}
		return result;
	}

	public MethodDeclaration findMethodByNameAndParametersAndReturn(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!apidiff2.internal.analysis.comparator.ComparatorMethod.isDiffMethodByNameAndParametersAndReturn(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}

	private MethodDeclaration findMethodByNameAndReturn(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!apidiff2.internal.analysis.comparator.ComparatorMethod.isDiffMethodByNameAndReturn(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}
	
	public MethodDeclaration findMethodByNameAndParameters(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!ComparatorMethod.isDiffMethodByNameAndParameters(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}

	public MethodDeclaration getEqualVersionMethod(MethodDeclaration method, TypeDeclaration type){
		for(MethodDeclaration methodInThisVersion : this.getAllEqualMethodsByName(method, type)){
			if(UtilTools.isEqualMethod(method, methodInThisVersion)){
				return methodInThisVersion;
			}
		}
		return null;
	}
	
	public EnumConstantDeclaration getEqualVersionConstant(EnumConstantDeclaration constant, EnumDeclaration enumReference) {
		EnumDeclaration thisVersionEnum = this.getVersionAccessibleEnum(enumReference);
		for(Object thisVersionConstant : thisVersionEnum.enumConstants()){
			if(((EnumConstantDeclaration)thisVersionConstant).getName().toString().equals(constant.getName().toString()))
				return ((EnumConstantDeclaration)thisVersionConstant);
		}

		return null;
	}
	
	public List<AbstractTypeDeclaration> getAllTypes(){
		List<AbstractTypeDeclaration> listTypesVersion = new ArrayList<AbstractTypeDeclaration>();
		listTypesVersion.addAll(this.getTypesPublicAndProtected());
		listTypesVersion.addAll(this.getTypesPrivateAndDefault());
		return listTypesVersion;
	}
	
}
