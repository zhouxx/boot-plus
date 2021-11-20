/*
 *    Copyright 2017-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.generate.definition;

import com.fasterxml.classmate.ResolvedType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ClassDefinition {

    //类的类型，是domain，mapper或service
    private ClassType classType;

    private String packageName;

    private Set<String> importList;

    //多行注释
    private List<String> comments;

    private String scope = "public";

    private String className;

    private List<String> extendClassList;

    private boolean interfaced;

    private boolean abstracted;

    private List<String> implementClassList;

    private List<String> annotationList;

    private List<FieldDefinition> fieldDefinitions;

    private List<MethodDefinition> methodDefinitions;

    public ClassType getClassType() {
        return classType;
    }

    public ClassDefinition setClassType(ClassType classType) {
        this.classType = classType;
        return this;
    }

    public ClassDefinition(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassDefinition setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public ClassDefinition setInterfaced(boolean interfaced) {
        this.interfaced = interfaced;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public ClassDefinition setAbstracted(boolean abstracted) {
        this.abstracted = abstracted;
        return this;
    }

    public ClassDefinition addImport(String importString) {
        if(importList == null) {
            this.importList = new TreeSet<>();
        }

        importList.add(importString);
        return this;
    }

    public ClassDefinition addImport(Class<?> importClass) {
        if(importClass.getTypeName().startsWith("java.lang")) {
            return this;
        }
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importClass.getName());
        return this;
    }

    public ClassDefinition addComment(String comment) {
        if(comments == null) {
            this.comments = new ArrayList<>();
        }

        comments.add(comment);
        return this;
    }

    public ClassDefinition addExtend(String extendClassName) {
        if(extendClassList == null) {
            this.extendClassList = new ArrayList<>();
        }
        extendClassList.add(extendClassName);
        return this;
    }

    public ClassDefinition addExtend(ResolvedType resolvedType) {
        String extendStr = resolvedType.getErasedType().getSimpleName();
        List<ResolvedType> typeParameters = resolvedType.getTypeBindings().getTypeParameters();
        if(typeParameters != null && typeParameters.size() > 0) {
            List<String> list = typeParameters.stream().map(resolvedType1 -> {
                this.addImport(resolvedType1.getErasedType());
                return resolvedType1.getErasedType().getSimpleName();
            }).collect(Collectors.toList());

            String join = String.join(", ", list);
            extendStr = extendStr + "<" + join + ">";

        }
        return this.addExtend(extendStr).addImport(resolvedType.getErasedType());
    }

    public ClassDefinition addExtend(Class<?> clazz) {
        return this.addExtend(clazz.getSimpleName()).addImport(clazz);
    }

    public ClassDefinition addImplement(String implementClassName) {
        if(implementClassList == null) {
            this.implementClassList = new ArrayList<>();
        }
        implementClassList.add(implementClassName);
        return this;
    }

    public ClassDefinition addImplement(Class<?> clazz) {
        return this.addImplement(clazz.getSimpleName()).addImport(clazz);
    }

    public ClassDefinition addAnnotation(String annotationName) {
        if(annotationList == null) {
            this.annotationList = new ArrayList<>();
        }
        annotationList.add(annotationName);

        return this;
    }

    public ClassDefinition addAnnotation(Class<?> clazz) {
        return this.addAnnotation(clazz.getSimpleName()).addImport(clazz);
    }

    public ClassDefinition addAnnotation(AnnotationDefinition annotationDefinition) {
        this.addAnnotation(annotationDefinition.toString());
        annotationDefinition.getImportList().forEach(str -> this.addImport(str));
        return this;
    }

    public ClassDefinition addFieldDefinition(FieldDefinition fieldDefinition) {
        if(fieldDefinitions == null) {
            this.fieldDefinitions = new ArrayList<>();
        }
        fieldDefinitions.add(fieldDefinition);
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        if(fieldDefinition.getImportList() != null) {
            this.importList.addAll(fieldDefinition.getImportList());
        }
        return this;
    }

    public ClassDefinition addMethodDefinition(MethodDefinition methodDefinition) {
        if(methodDefinitions == null) {
            this.methodDefinitions = new ArrayList<>();
        }
        methodDefinitions.add(methodDefinition);
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        if(methodDefinition.getImportList() != null) {
            this.importList.addAll(methodDefinition.getImportList());
        }
        return this;
    }

    public List<MethodDefinition> getMethodDefinitions() {
        return methodDefinitions;
    }

    public void out(OutputStream outputStream) throws IOException {
        byte[] bytes = this.toString().getBytes();
        outputStream.write(bytes);
    }

    @Override
    public String toString() {
        String lineEnd = "\r\n";
        StringBuilder builder = new StringBuilder();
        //package part
        if(packageName != null) {
            builder.append("package " + packageName + ";" + lineEnd);
        }
        builder.append(lineEnd);

        //import part
        if(importList != null) {
            importList.forEach(s -> builder.append("import " + s + ";" + lineEnd));
        }
        builder.append(lineEnd);

        //comment part
        if(comments != null) {
            builder.append("/**" + lineEnd);
            comments.forEach(s -> {
                builder.append(" * " + s + lineEnd);
            });
            builder.append(" */" + lineEnd);
        }

        //annotation part
        if(annotationList != null) {
            annotationList.forEach(s -> {
                builder.append("@" + s + lineEnd);
            });
        }

        //class part
        String classType = "class";
        if(interfaced) {
            classType = "interface";
        }

        builder.append(scope + (abstracted ? " abstract " : " ") + classType + " " + className);

        if(extendClassList != null) {
            builder.append(" extends ");
            builder.append(String.join(", ", extendClassList));
        }

        if(implementClassList != null) {
            builder.append(" implements ");
            builder.append(String.join(", ", implementClassList));
        }
        builder.append(" {" + lineEnd);
        builder.append(lineEnd);

        String space4 = "\t";

        //field part
        if(fieldDefinitions != null) {
            for(FieldDefinition fieldDefinition : fieldDefinitions) {

                if(fieldDefinition.getComments() != null) {
                    builder.append(space4).append("/**").append(lineEnd);
                    fieldDefinition.getComments().forEach(s -> builder.append(space4 + " * " + s + lineEnd));
                    builder.append(space4 + " */" + lineEnd);
                }

                if(fieldDefinition.getAnnotationList() != null) {
                    fieldDefinition.getAnnotationList().forEach(s -> builder.append(space4).append("@").append(s).append(lineEnd));
                }
                builder.append(space4 + fieldDefinition.getScope() + " " + fieldDefinition.getType() + " " + fieldDefinition.getName() + ";" + lineEnd);
                builder.append(lineEnd);
            }
        }

        //method part
        if(methodDefinitions != null) {
            for(MethodDefinition methodDefinition : methodDefinitions) {
                if(methodDefinition.getAnnotationList() != null) methodDefinition.getAnnotationList().forEach(s -> {
                    builder.append(space4).append("@").append(s).append(lineEnd);
                });
                builder.append(space4).append(methodDefinition.getScope()).append(" ").append(methodDefinition.getReturnValue()).append(" ").append(methodDefinition.getMethodName()).append("(");
                if(methodDefinition.getParameters() != null) {
                    for (int i = 0; i < methodDefinition.getParameters().size(); i++) {
                        ParameterDefinition parameterDefinition = methodDefinition.getParameters().get(i);
                        if(i > 0) {
                            builder.append(", ");
                        }
                        if(parameterDefinition.getAnnotationList()!= null) {
                            parameterDefinition.getAnnotationList().forEach(s -> builder.append("@").append(s).append(" "));
                        }
                        builder.append(parameterDefinition.getType()).append(" ").append(parameterDefinition.getName());
                    }
                }
                if(methodDefinition.isHasBody()) {
                    builder.append(")" + " {").append(lineEnd);
                    if(methodDefinition.getBodyLines() != null) {
                        methodDefinition.getBodyLines().forEach(s -> builder.append(space4).append(space4).append(s).append(lineEnd));
                    }
                    builder.append(space4).append("}").append(lineEnd);
                } else {
                    builder.append(");").append(lineEnd);
                }

                builder.append(lineEnd);
            }
        }

        builder.append("}").append(lineEnd);

        return builder.toString();
    }

}
