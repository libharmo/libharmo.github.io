package apidiff2.internal.analysis;

import apidiff2.Result;
import apidiff2.internal.refactor.RefactorProcessor;
import apidiff2.internal.refactor.RefactoringProcessorImpl;
import apidiff2.internal.visitor.APIVersion;
import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.List;
import java.util.Map;

public class DiffProcessorImpl {

	public Result detectChange(final APIVersion version1, final APIVersion version2, String prevDecomPath, String currDecomPath) {
		Result result = new Result();
		Map<RefactoringType, List<SDRefactoring>> refactorings = this.detectRefactoring(prevDecomPath,currDecomPath);
		result.getChangeType().addAll(new TypeDiff().detectChange(version1, version2, refactorings));
		result.getChangeMethod().addAll(new MethodDiff().detectChange(version1, version2, refactorings));
		result.getChangeField().addAll(new FieldDiff().detectChange(version1, version2, refactorings));
		return result;
	}

	public Map<RefactoringType, List<SDRefactoring>> detectRefactoring(String prevDir, String currDir) {
		RefactorProcessor refactoringDetector = new RefactoringProcessorImpl();
		return refactoringDetector.detectRefactoring(prevDir,currDir);
	}

}
