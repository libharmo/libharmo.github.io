package apidiff2.internal.analysis;

import apidiff2.Result;
import apidiff2.internal.visitor.APIVersion;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.refactoring.SDRefactoring;

import java.util.List;
import java.util.Map;

public interface DiffProcessor {
	
	public Map<RefactoringType, List<SDRefactoring>> detectRefactoring(final Repository repository, final String commit);
	
	public Result detectChange(final APIVersion version1, final APIVersion version2, final Repository repository, final RevCommit revCommit);

}
