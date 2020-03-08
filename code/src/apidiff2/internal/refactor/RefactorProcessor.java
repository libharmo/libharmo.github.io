package apidiff2.internal.refactor;

import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.List;
import java.util.Map;

public interface RefactorProcessor {
	
//	public Map<RefactoringType, List<SDRefactoring>> detectRefactoringAtCommit (final Repository repository, final String commit);

	public Map<RefactoringType, List<SDRefactoring>> detectRefactoring(String a, String b);

}
