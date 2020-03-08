package apidiff2;

import apidiff2.enums.Classifier;
import apidiff2.internal.analysis.DiffProcessorImpl;
import apidiff2.internal.visitor.APIVersion;
import cn.edu.fudan.se.jardiff.JarDiff;
import cn.edu.fudan.se.util.JavaMethodUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class APIDiff2 {
	
	private String prevLib;
	private String currLib;
	private String outputPath;

	private String prevLibDecomPath;
	private String currLibDecomPath;
	
	private Logger logger = LoggerFactory.getLogger(APIDiff2.class);
	public APIDiff2(final String prevLib, final String currLib) {
		this.prevLib = prevLib;
		this.currLib = currLib;
	}
	public String getPath() {
		return outputPath;
	}
	public void setPath(String path) {
		this.outputPath = path;
	}
	public void initJars(){
		this.prevLibDecomPath = JavaMethodUtil.decompileJar(this.prevLib,outputPath);
		this.currLibDecomPath = JavaMethodUtil.decompileJar(this.currLib,outputPath);

	}

	public apidiff2.Result detectChange(Classifier classifierAPI) {
		apidiff2.Result result = new apidiff2.Result();
		try {
			apidiff2.Result resultByClassifier = this.diffJar(classifierAPI);
			result.getChangeType().addAll(resultByClassifier.getChangeType());
			result.getChangeMethod().addAll(resultByClassifier.getChangeMethod());
			result.getChangeField().addAll(resultByClassifier.getChangeField());
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	private apidiff2.Result diffJar(Classifier classifierAPI){
//		File projectFolder = new File(UtilTools.getPathProject(this.currLib, nameProject));
		try {
			Map<DiffEntry.ChangeType, List<String>> diffInfo = JarDiff.diffMeta(prevLibDecomPath,currLibDecomPath);
			List<String> modify = diffInfo.get(DiffEntry.ChangeType.MODIFY);
			APIVersion version1 = this.getAPIVersion(prevLibDecomPath,modify, classifierAPI);//old version
			APIVersion version2 = this.getAPIVersion(currLibDecomPath,modify, classifierAPI); //new version
			DiffProcessorImpl diff = new DiffProcessorImpl();
			return diff.detectChange(version1, version2,prevLibDecomPath,currLibDecomPath);
		} catch (Exception e) {
//			this.logger.error("Error");
			e.printStackTrace();
		}
		return new Result();
	}
	
	private APIVersion getAPIVersion(String path, List<String> modify, Classifier classifierAPI) throws Exception {
//		GitService service = new GitServiceImpl();
		//Finding changed files between current commit and parent commit.
//		Map<ChangeType, List<GitFile>> mapModifications = service.fileTreeDiff(repository, currentCommit);
//		service.checkout(repository, commit);
		return new APIVersion(path, modify, classifierAPI);
	}

}
