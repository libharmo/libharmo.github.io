package apidiff2.internal.refactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import refdiff.core2.RefDiff;
import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefactoringProcessorImpl implements RefactorProcessor {
	
	private Logger logger = LoggerFactory.getLogger(RefactoringProcessorImpl.class);
	
	private Map<RefactoringType, List<SDRefactoring>> format(final List<SDRefactoring> refactorings){
		Map<RefactoringType, List<SDRefactoring>> result = new HashMap<RefactoringType, List<SDRefactoring>>();
		for(SDRefactoring ref : refactorings){
			RefactoringType refactoringName =  ref.getRefactoringType();
			if(result.containsKey(refactoringName)){
				result.get(refactoringName).add(ref);
			}
			else{
				List<SDRefactoring> listRefactorings = new ArrayList<SDRefactoring>();
				listRefactorings.add(ref);
				result.put(refactoringName, listRefactorings);
			}
		}
		return result;
	}
	
//	@Override
//	public Map<RefactoringType, List<SDRefactoring>> detectRefactoring(final Repository repository, final String commit){
//		Map<RefactoringType, List<SDRefactoring>> result = new HashMap<RefactoringType, List<SDRefactoring>>();
//		try{
//			RefDiff refDiff = new RefDiff();
//			result = this.format(refDiff.detect(repository, commit));
//		} catch (Exception e) {
//			this.logger.error("Erro in refactoring process [repository=" + repository + "][commit=" + commit + "]", e);
//		}
//		return result;
//	}

	public Map<RefactoringType, List<SDRefactoring>> detectRefactoring(String a, String b){
		Map<RefactoringType, List<SDRefactoring>> result = new HashMap<RefactoringType, List<SDRefactoring>>();
		try{
			RefDiff refDiff = new RefDiff();
			result = this.format(refDiff.detect(a,b));
		} catch (Exception e) {

		}
		return result;
	}

}
