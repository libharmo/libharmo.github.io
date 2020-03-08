package refdiff.core2.rm2.analysis;

import cn.edu.fudan.se.jardiff.JarDiff;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import refdiff.core2.api.GitService;
import refdiff.core2.rm2.model.SDModel;

import java.io.File;
import java.util.*;

public class GitHistoryStructuralDiffAnalyzer {

	Logger logger = LoggerFactory.getLogger(GitHistoryStructuralDiffAnalyzer.class);
	private final refdiff.core2.rm2.analysis.RefDiffConfig config;
	
	public GitHistoryStructuralDiffAnalyzer() {
        this(new RefDiffConfigImpl());
    }
	
	public GitHistoryStructuralDiffAnalyzer(RefDiffConfig config) {
        this.config = config;
    }

    private void detect(GitService gitService, Repository repository, final StructuralDiffHandler handler, Iterator<RevCommit> i) {
		int commitsCount = 0;
		int errorCommitsCount = 0;

		File metadataFolder = repository.getDirectory();
		File projectFolder = metadataFolder.getParentFile();
		String projectName = projectFolder.getName();
		
		long time = System.currentTimeMillis();
		while (i.hasNext()) {
			RevCommit currentCommit = i.next();
			try {
//				detectRefactorings(gitService, repository, handler, projectFolder, currentCommit);
				
			} catch (Exception e) {
				logger.warn(String.format("Ignored revision %s due to error", currentCommit.getId().getName()), e);
				handler.handleException(currentCommit.getId().getName(), e);
				errorCommitsCount++;
			}

			commitsCount++;
			long time2 = System.currentTimeMillis();
			if ((time2 - time) > 20000) {
				time = time2;
				logger.info(String.format("Processing %s [Commits: %d, Errors: %d]", projectName, commitsCount, errorCommitsCount));
			}
		}

		handler.onFinish(commitsCount, errorCommitsCount);
		logger.info(String.format("Analyzed %s [Commits: %d, Errors: %d]", projectName, commitsCount, errorCommitsCount));
	}

	public void detectAll(Repository repository, String branch, final StructuralDiffHandler handler) throws Exception {
		GitService gitService = new refdiff.core2.util.GitServiceImpl() {
			@Override
			public boolean isCommitAnalyzed(String sha1) {
				return handler.skipCommit(sha1);
			}
		};
		RevWalk walk = gitService.createAllRevsWalk(repository, branch);
		try {
			detect(gitService, repository, handler, walk.iterator());
		} finally {
			walk.dispose();
		}
	}

	public void fetchAndDetectNew(Repository repository, final StructuralDiffHandler handler) throws Exception {
		GitService gitService = new refdiff.core2.util.GitServiceImpl() {
			@Override
			public boolean isCommitAnalyzed(String sha1) {
				return handler.skipCommit(sha1);
			}
		};
		RevWalk walk = gitService.fetchAndCreateNewRevsWalk(repository);
		try {
			detect(gitService, repository, handler, walk.iterator());
		} finally {
			walk.dispose();
		}
	}

	public void detect(String a, String b, StructuralDiffHandler handler) {
//		File metadataFolder = repository.getDirectory();
//		File projectFolder = metadataFolder.getParentFile();
//		GitService gitService = new GitServiceImpl();
		//RevWalk walk = new RevWalk(repository);
//		try (RevWalk walk = new RevWalk(repository)) {
//			RevCommit commit = walk.parseCommit(repository.resolve(commitId));
//			if (commit.getParentCount() == 1) {
//			    walk.parseCommit(commit.getParent(0));
		try {
			this.detectRefactorings(a, b, handler);
		}catch(Exception e){
			e.printStackTrace();
		}
//			}
//		} catch (Exception e) {
//		    logger.warn(String.format("Ignored revision %s due to error", commitId), e);
//		    handler.handleException(commitId, e);
//        }
	}
	
	protected void detectRefactorings(String a, String b, final StructuralDiffHandler handler) throws Exception {
//	    String commitId = currentCommit.getId().getName();

//		gitService.fileTreeDiff(repository, currentCommit, filesBefore, filesCurrent, renamedFilesHint, false);
		// If no java files changed, there is no refactoring. Also, if there are
		// only ADD's or only REMOVE's there is no refactoring
		List<String> filesBefore = new ArrayList<String>();
		List<String> filesCurrent = new ArrayList<String>();
		Map<String, String> renamedFilesHint = new HashMap<String, String>();
		refdiff.core2.rm2.analysis.SDModelBuilder builder = new SDModelBuilder(config);
		if (filesBefore.isEmpty() || filesCurrent.isEmpty()) {
		    return;
		}
		// filesBefore filesAfter
		// filesAfter filesBefore
		Map<DiffEntry.ChangeType,List<String>> changeList = JarDiff.diffMeta(a,b);
		filesBefore.addAll(changeList.get(DiffEntry.ChangeType.DELETE));
		filesBefore.addAll(changeList.get(DiffEntry.ChangeType.MODIFY));
		filesCurrent.addAll(changeList.get(DiffEntry.ChangeType.ADD));
		filesCurrent.addAll(changeList.get(DiffEntry.ChangeType.MODIFY));

		File folderAfter = new File(b);
		File folderBefore = new File(a);
		builder.analyzeAfter(folderAfter, filesCurrent);
		builder.analyzeBefore(folderBefore, filesBefore);
		final SDModel model = builder.buildModel();
		handler.handle(null, model);
	// Checkout and build model for current commit
//	    File folderAfter = new File(projectFolder.getParentFile(), "v1/" + projectFolder.getName() + "-" + commitId.substring(0, 7));
//	    if (folderAfter.exists()) {
//		logger.info(String.format("Analyzing code after (%s) ..."));

//	    } else {
//	        gitService.checkout(repository, commitId);
//	        logger.info(String.format("Analyzing code after (%s) ...", commitId));
//	        builder.analyzeAfter(projectFolder, filesCurrent);
//	    }
	
//	    String parentCommit = currentCommit.getParent(0).getName();
//		File folderBefore = new File(projectFolder.getParentFile(), "v0/" + projectFolder.getName() + "-" + commitId.substring(0, 7));
//		if (folderBefore.exists()) {
//		logger.info(String.format("Analyzing code before (%s) ...", parentCommit));
//		} else {
		    // Checkout and build model for parent commit
//		    gitService.checkout(repository, parentCommit);
//		    logger.info(String.format("Analyzing code before (%s) ...", parentCommit));
//		    builder.analyzeBefore(projectFolder, filesBefore);
//		}
//		}

	}

}
