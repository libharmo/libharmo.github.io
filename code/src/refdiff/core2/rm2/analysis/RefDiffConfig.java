package refdiff.core2.rm2.analysis;

import refdiff.core2.rm2.analysis.codesimilarity.CodeSimilarityStrategy;
import refdiff.core2.rm2.model.RelationshipType;

public interface RefDiffConfig {

    String getId();

    double getThreshold(RelationshipType relationshipType);

    CodeSimilarityStrategy getCodeSimilarityStrategy();

}