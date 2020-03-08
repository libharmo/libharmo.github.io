package refdiff.core2.rm2.analysis;

import org.eclipse.jdt.core.dom.ASTNode;
import refdiff.core2.rm2.model.SDEntity;
import refdiff.core2.rm2.model.SourceRepresentation;

import java.util.List;

public interface SourceRepresentationBuilder {

    SourceRepresentation buildSourceRepresentation(SDEntity entity, char[] charArray, ASTNode astNode);

    SourceRepresentation buildPartialSourceRepresentation(char[] charArray, ASTNode astNode);

    SourceRepresentation buildSourceRepresentation(SDEntity entity, List<SourceRepresentation> parts);

    SourceRepresentation buildEmptySourceRepresentation();

    default void onComplete() {}

}