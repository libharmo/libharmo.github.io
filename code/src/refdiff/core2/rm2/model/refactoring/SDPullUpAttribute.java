package refdiff.core2.rm2.model.refactoring;

import refdiff.core2.api.RefactoringType;
import refdiff.core2.rm2.model.SDAttribute;

public class SDPullUpAttribute extends SDMoveAttribute {

    public SDPullUpAttribute(SDAttribute attributeBefore, SDAttribute attributeAfter) {
        super(RefactoringType.PULL_UP_ATTRIBUTE, attributeBefore, attributeAfter);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName());
        sb.append(' ');
        sb.append(attributeAfter.getVerboseSimpleName());
        sb.append(" from class ");
        sb.append(attributeBefore.container().fullName());
        sb.append(" to class ");
        sb.append(attributeAfter.container().fullName());
        return sb.toString();
    }
}
