package refdiff.core2.rm2.analysis;

import org.eclipse.jdt.core.dom.*;
import refdiff.core2.rm2.model.*;
import refdiff.core2.util.AstUtils;

import java.util.*;

public class BindingsRecoveryAstVisitor extends ASTVisitor {

    private final SDModel.Snapshot model;
    private final String sourceFilePath;
    private final char[] fileContent;
    private final LinkedList<SDContainerEntity> containerStack;
    private final Map<SDEntity, List<String>> postProcessReferences;
    private final Map<SDType, List<String>> postProcessSupertypes;
    private final Map<String, List<SourceRepresentation>> postProcessClientCode;
    private final refdiff.core2.rm2.analysis.SourceRepresentationBuilder srbForTypes;
    private final refdiff.core2.rm2.analysis.SourceRepresentationBuilder srbForMethods;
    private final refdiff.core2.rm2.analysis.SourceRepresentationBuilder srbForAttributes;

    public BindingsRecoveryAstVisitor(SDModel.Snapshot model, String sourceFilePath, char[] fileContent, SDPackage sdPackage, Map<SDEntity, List<String>> postProcessReferences,
                                      Map<SDType, List<String>> postProcessSupertypes, Map<String, List<SourceRepresentation>> postProcessClientCode, refdiff.core2.rm2.analysis.SourceRepresentationBuilder srbForTypes, refdiff.core2.rm2.analysis.SourceRepresentationBuilder srbForMethods, SourceRepresentationBuilder srbForAttributes) {
        this.model = model;
        this.sourceFilePath = sourceFilePath;
        this.fileContent = fileContent;
        this.containerStack = new LinkedList<SDContainerEntity>();
        this.containerStack.push(sdPackage);
        this.postProcessReferences = postProcessReferences;
        this.postProcessSupertypes = postProcessSupertypes;
        this.postProcessClientCode = postProcessClientCode;
        this.srbForTypes = srbForTypes;
        this.srbForMethods = srbForMethods;
        this.srbForAttributes = srbForAttributes;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        if (node.getParent() instanceof ClassInstanceCreation) {
            ClassInstanceCreation parent = (ClassInstanceCreation) node.getParent();
            ITypeBinding typeBinding = parent.getType().resolveBinding();
            if (typeBinding != null && typeBinding.isFromSource()) {
                SDType type = model.createAnonymousType(containerStack.peek(), sourceFilePath, "");
                containerStack.push(type);
                extractSupertypesForPostProcessing(type, typeBinding);
                return true;
            }
        }
        return false;
    }

    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        if (node.getParent() instanceof ClassInstanceCreation) {
            ClassInstanceCreation parent = (ClassInstanceCreation) node.getParent();
            ITypeBinding typeBinding = parent.getType().resolveBinding();
            if (typeBinding != null && typeBinding.isFromSource()) {
                containerStack.pop();
            }
        }
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        containerStack.push(visitTypeDeclaration(node, node.superInterfaceTypes()));
        return true;
    }

    public void endVisit(EnumDeclaration node) {
        containerStack.pop();
    }

    public boolean visit(TypeDeclaration typeDeclaration) {
        List<Type> supertypes = new ArrayList<Type>();
        Type superclass = typeDeclaration.getSuperclassType();
        if (superclass != null) {
            supertypes.add(superclass);
        }
        supertypes.addAll(typeDeclaration.superInterfaceTypes());
        SDType sdType = visitTypeDeclaration(typeDeclaration, supertypes);
        containerStack.push(sdType);
        sdType.setIsInterface(typeDeclaration.isInterface());
        return true;
    }

    public void endVisit(TypeDeclaration node) {
        containerStack.pop();
    }

    private SDType visitTypeDeclaration(AbstractTypeDeclaration node, List<Type> supertypes) {
        SDType type;
        String typeName = node.getName().getIdentifier();
        if (node.isLocalTypeDeclaration()) {
            type = model.createAnonymousType(containerStack.peek(), sourceFilePath, typeName);
        } else {
            type = model.createType(typeName, containerStack.peek(), sourceFilePath);
        }
        //type.setSourceCode(srbForTypes.buildSourceRepresentation(fileContent, node.getStartPosition(), node.getLength()));
        type.setSourceCode(srbForTypes.buildSourceRepresentation(type, fileContent, node));

        Set<String> annotations = extractAnnotationTypes(node.modifiers());
        type.setDeprecatedAnnotation(annotations.contains("Deprecated"));

        for (Type superType : supertypes) {
            ITypeBinding superTypeBinding = superType.resolveBinding();
            extractSupertypesForPostProcessing(type, superTypeBinding);
        }

//        final List<String> references = new ArrayList<String>();
//        node.accept(new DependenciesAstVisitor(true) {
//            @Override
//            protected void onTypeAccess(ASTNode node, ITypeBinding binding) {
//                String typeKey = AstUtils.getKeyFromTypeBinding(binding);
//                references.add(typeKey);
//            }
//        });
//        postProcessReferences.put(type, references);

        return type;
    }

    private void extractSupertypesForPostProcessing(SDType type, ITypeBinding superTypeBinding) {
        List<String> supertypes = postProcessSupertypes.get(type);
        if (supertypes == null) {
            supertypes = new ArrayList<String>();
            postProcessSupertypes.put(type, supertypes);
        }
        while (superTypeBinding != null && superTypeBinding.isFromSource()) {
            String superTypeName = superTypeBinding.getErasure().getQualifiedName();
            supertypes.add(superTypeName);
            superTypeBinding = superTypeBinding.getSuperclass();
        }
    }

    public boolean visit(MethodDeclaration methodDeclaration) {
        // ASTNode parentNode = methodDeclaration.getParent();
        // if (!(parentNode instanceof TypeDeclaration)) {
        // // ignore methods from anonymous classes
        // return false;
        // }
        String methodSignature = refdiff.core2.util.AstUtils.getSignatureFromMethodDeclaration(methodDeclaration);
        // if
        // (methodDeclaration.getName().getIdentifier().equals("execMultiLineCommands"))
        // {
        // System.out.println("x");
        //
        // }

        final SDMethod method = model.createMethod(methodSignature, containerStack.peek(), methodDeclaration.isConstructor());

        List<?> modifiers = methodDeclaration.modifiers();
        Set<String> annotations = extractAnnotationTypes(modifiers);
        method.setTestAnnotation(annotations.contains("Test"));
        method.setDeprecatedAnnotation(annotations.contains("Deprecated") || refdiff.core2.util.AstUtils.containsDeprecatedTag(methodDeclaration.getJavadoc()));

        int methodModifiers = methodDeclaration.getModifiers();
        Visibility visibility = getVisibility(methodModifiers);

        method.setVisibility(visibility);
        extractParametersAndReturnType(methodDeclaration, method);

        method.setNumberOfStatements(refdiff.core2.util.AstUtils.countNumberOfStatements(methodDeclaration));
        Block body = methodDeclaration.getBody();
        if (body == null) {
            method.setSourceCode(srbForMethods.buildEmptySourceRepresentation());
            method.setAbstract(true);
        } else {
            //method.setSourceCode(srbForMethods.buildSourceRepresentation(this.fileContent, body.getStartPosition() + 1, body.getLength() - 2));
            method.setSourceCode(srbForMethods.buildSourceRepresentation(method, this.fileContent, body));
            final List<String> references = new ArrayList<String>();
            body.accept(new DependenciesAstVisitor(true) {
                @Override
                protected void onMethodAccess(ASTNode node, IMethodBinding binding) {
                    String methodKey = refdiff.core2.util.AstUtils.getKeyFromMethodBinding(binding);
                    references.add(methodKey);
                }

                @Override
                protected void onFieldAccess(ASTNode node, IVariableBinding binding) {
                    String attributeKey = refdiff.core2.util.AstUtils.getKeyFromFieldBinding(binding);
                    references.add(attributeKey);

                    Statement stm = refdiff.core2.util.AstUtils.getEnclosingStatement(node);
                    // if (stm == null) {
                    // System.out.println("null");
                    // }
                    SourceRepresentation code = srbForAttributes.buildPartialSourceRepresentation(fileContent, stm);
                    addClientCode(attributeKey, code);
                }

            });
            postProcessReferences.put(method, references);
        }

        return true;
    }

    private void addClientCode(String attributeKey, SourceRepresentation code) {
        List<SourceRepresentation> codeFragments = postProcessClientCode.get(attributeKey);
        if (codeFragments == null) {
            codeFragments = new ArrayList<SourceRepresentation>();
        }
        codeFragments.add(code);
        postProcessClientCode.put(attributeKey, codeFragments);
    }

    private Visibility getVisibility(int methodModifiers) {
        Visibility visibility;
        if ((methodModifiers & Modifier.PUBLIC) != 0)
            visibility = Visibility.PUBLIC;
        else if ((methodModifiers & Modifier.PROTECTED) != 0)
            visibility = Visibility.PROTECTED;
        else if ((methodModifiers & Modifier.PRIVATE) != 0)
            visibility = Visibility.PRIVATE;
        else
            visibility = Visibility.PACKAGE;
        return visibility;
    }

    @Override
    public boolean visit(FieldDeclaration fieldDeclaration) {
        Type fieldType = fieldDeclaration.getType();
        int fieldModifiers = fieldDeclaration.getModifiers();
        Visibility visibility = getVisibility(fieldModifiers);
        // boolean isFinal = (fieldModifiers & Modifier.FINAL) != 0;
        boolean isStatic = (fieldModifiers & Modifier.STATIC) != 0;
        List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
            String fieldName = fragment.getName().getIdentifier();
            final SDAttribute attribute = model.createAttribute(fieldName, containerStack.peek());
            attribute.setStatic(isStatic);
            attribute.setVisibility(visibility);
            attribute.setType(refdiff.core2.util.AstUtils.normalizeTypeName(fieldType, fragment.getExtraDimensions(), false));

            Expression expression = fragment.getInitializer();
            if (expression != null) {
                //attribute.setAssignment(srbForAttributes.buildSourceRepresentation(fileContent, expression.getStartPosition(), expression.getLength()));
                addClientCode(attribute.key().toString(), srbForAttributes.buildPartialSourceRepresentation(fileContent, expression));
            }
            attribute.setClientCode(srbForAttributes.buildEmptySourceRepresentation());
        }
        return true;
    }

    private static Set<String> extractAnnotationTypes(List<?> modifiers) {
        Set<String> annotations = new HashSet<String>();
        for (Object modifier : modifiers) {
            if (modifier instanceof Annotation) {
                Annotation a = (Annotation) modifier;
                annotations.add(a.getTypeName().toString());
            }
        }
        return annotations;
    }

    public static void extractParametersAndReturnType(MethodDeclaration methodDeclaration, SDMethod method) {
        Type returnType = methodDeclaration.getReturnType2();
        if (returnType != null) {
            method.setReturnType(refdiff.core2.util.AstUtils.normalizeTypeName(returnType, methodDeclaration.getExtraDimensions(), false));
        } else {
            method.setReturnType(null);
        }
        Iterator<SingleVariableDeclaration> parameters = methodDeclaration.parameters().iterator();
        while (parameters.hasNext()) {
            SingleVariableDeclaration parameter = parameters.next();
            Type parameterType = parameter.getType();
            String typeName = AstUtils.normalizeTypeName(parameterType, parameter.getExtraDimensions(), parameter.isVarargs());
            method.addParameter(parameter.getName().getIdentifier(), typeName);
        }
    }

    // @Override
    // public void endVisit(MethodDeclaration methodDeclaration) {
    // if
    // (methodDeclaration.getName().toString().endsWith("testFixedMembershipTokenIPv4"))
    // {
    // System.out.print(' ');
    // }
    // }

    // private String getTypeName(Type type) {
    // ITypeBinding binding = type.resolveBinding();
    // ITypeBinding y = binding;
    // while (y != null && y.isFromSource()) {
    // System.out.println(y.getQualifiedName());
    // y = y.getSuperclass();
    // }
    //
    // if (binding != null) {
    // return binding.getQualifiedName();
    // }
    // return type.toString();
    // }

}
