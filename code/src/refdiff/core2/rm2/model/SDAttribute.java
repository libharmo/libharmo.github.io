package refdiff.core2.rm2.model;


public class SDAttribute extends SDEntity {

	private final String name; 
	private refdiff.core2.rm2.model.Visibility visibility;
    private String type;
    private boolean isStatic;
    private refdiff.core2.rm2.model.Multiset<refdiff.core2.rm2.model.SDMethod> referencedBy;
    //private SourceRepresentation assignment;
    private refdiff.core2.rm2.model.SourceRepresentation clientCode;
	
	public SDAttribute(SDModel.Snapshot snapshot, int id, String name, SDContainerEntity container) {
		super(snapshot, id, container.fullName() + "#" + name, new EntityKey(container.key() + "#" + name), container);
		this.name = name;
		this.referencedBy = new refdiff.core2.rm2.model.Multiset<refdiff.core2.rm2.model.SDMethod>();
	}

	@Override
	public String simpleName() {
		return name;
	}
	
	@Override
	public boolean isTestCode() {
		return container.isTestCode();
	}

	@Override
	protected final String getNameSeparator() {
		return "#";
	}

    public refdiff.core2.rm2.model.Visibility visibility() {
        return visibility;
    }

    public String type() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public refdiff.core2.rm2.model.SDType container() {
        return (SDType) this.container;
    }
    
    public refdiff.core2.rm2.model.SourceRepresentation clientCode() {
        return clientCode;
    }

    public Multiset<refdiff.core2.rm2.model.SDMethod> referencedBy() {
        return referencedBy;
    }

    @Override
    public void addReferencedBy(SDMethod method) {
        this.referencedBy.add(method);
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public void setClientCode(SourceRepresentation clientCode) {
        this.clientCode = clientCode;
    }

//    public SourceRepresentation assignment() {
//        return assignment;
//    }
//
//    public void setAssignment(SourceRepresentation assignment) {
//        this.assignment = assignment;
//    }

    public String getVerboseSimpleName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.visibility);
        sb.append(' ');
        sb.append(name);
        sb.append(" : ");
        sb.append(type);
        return sb.toString();
    }

    @Override
    public boolean isAnonymous() {
        return container().isAnonymous();
    }
}
