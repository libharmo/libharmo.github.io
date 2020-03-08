package refdiff.core2.rm2.analysis.codesimilarity;

import refdiff.core2.rm2.analysis.SourceRepresentationBuilder;

public interface CodeSimilarityStrategy {

    SourceRepresentationBuilder createSourceRepresentationBuilderForTypes();

    SourceRepresentationBuilder createSourceRepresentationBuilderForMethods();

    SourceRepresentationBuilder createSourceRepresentationBuilderForAttributes();

    public static CodeSimilarityStrategy BIGRAM = new CodeSimilarityStrategy() {
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForTypes() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder(refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder.LINES);
        }
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForMethods() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder(refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder.TOKENS);
        }
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForAttributes() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder(refdiff.core2.rm2.analysis.codesimilarity.TokenBigramsSRBuilder.TOKENS);
        }
    };

    public static CodeSimilarityStrategy TFIDF = new CodeSimilarityStrategy() {
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForTypes() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenIdfSRBuilder();
        }
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForMethods() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenIdfSRBuilder();
        }
        @Override
        public SourceRepresentationBuilder createSourceRepresentationBuilderForAttributes() {
            return new refdiff.core2.rm2.analysis.codesimilarity.TokenIdfSRBuilder();
        }
    };

}
