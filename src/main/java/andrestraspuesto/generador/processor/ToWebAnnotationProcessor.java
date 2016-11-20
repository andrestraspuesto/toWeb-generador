package andrestraspuesto.generador.processor;



import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.Processor;
import javax.annotation.processing.ProcessingEnvironment;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.VariableElement;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.util.Types;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;
import java.lang.annotation.Annotation;
import javax.tools.Diagnostic.Kind;
import javax.lang.model.util.Elements;


import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.io.Writer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.Collection;
import java.util.List;

import andrestraspuesto.generador.annotations.ToWeb;
import andrestraspuesto.generador.helpers.CodeWriterHelper;
import andrestraspuesto.generador.helpers.ProcessorHelper;
import andrestraspuesto.generador.dto.FieldInfoDto;



@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("andrestraspuesto.generador.annotations.ToWeb")
public class ToWebAnnotationProcessor extends AbstractProcessor {

	
    
    private Set<String> originals;

    
    @Override
    public void init (ProcessingEnvironment processingEnv) {
        super.init( processingEnv );
    }
	
	@Override
	public boolean process(Set<? extends TypeElement> annotation,  RoundEnvironment roundEnv){
		System.out.println("\n*******************processing ToWebAnnotationProcessor *****************\n");
		fillOriginals(roundEnv);
		boolean result = true;
		for(Element elem: roundEnv.getElementsAnnotatedWith(ToWeb.class)){
			if(elem instanceof TypeElement){
				TypeElement tElem = (TypeElement) elem;
				ToWeb toWeb = tElem.getAnnotation(ToWeb.class);

				if( !toWeb.dtoPackage().isEmpty()){
					//Preparo imports y fields					
					Set<String> importSet = new HashSet<>();
					Map<String, FieldInfoDto> fieldNamesMap = new HashMap<>();
					composeImportsAndFields(tElem, importSet, fieldNamesMap);

					result &= createDtoSrc(toWeb, tElem, importSet, fieldNamesMap);
					if(!toWeb.assemblerPackage().isEmpty()){
						result &= createAssemblerSrc(toWeb, tElem, importSet, fieldNamesMap);
					}
				}

			}
		}
		return result;
	}

	private boolean createDtoSrc(ToWeb toWeb, TypeElement tElem, Set<String> importSet, Map<String, FieldInfoDto> fieldNamesMap){
		boolean result = true;
		String curPack = toWeb.dtoPackage();
		String currSuf = toWeb.dtoSuffix();
		String className = curPack + "." + tElem.getSimpleName() + currSuf ;	
		//No modifico las fuentes originales para evitar problemas con el flujo 
		if(!isOriginal(className)){
			System.out.println( "Iniciando la creacion de " + className);
			try(Writer w = processingEnv.getFiler().createSourceFile(className, tElem).openWriter()){
				w.append(composeDto(tElem, toWeb, importSet, fieldNamesMap));
			}catch(IOException ioe){
				processingEnv.getMessager().printMessage(Kind.ERROR, ioe.getMessage());
				result = false;
			}
		}
		return result;
	}

	private boolean createAssemblerSrc(ToWeb toWeb, TypeElement tElem, Set<String> importSet, Map<String, FieldInfoDto> fieldNamesMap){
		boolean result = true;
		String curPack = toWeb.assemblerPackage();
		String currSuf = toWeb.assemblerSuffix();
		String className = curPack + "." + tElem.getSimpleName() + currSuf ;	
		//No modifico las fuentes originales para evitar problemas con el flujo 
		if(!isOriginal(className)){
			System.out.println( "Iniciando la creacion de " + className);
			try(Writer w = processingEnv.getFiler().createSourceFile(className, tElem).openWriter()){
				w.append(composeAssembler(tElem, toWeb, importSet, fieldNamesMap));
			}catch(IOException ioe){
				processingEnv.getMessager().printMessage(Kind.ERROR, ioe.getMessage());
				result = false;
			}
		}
		return result;
	}

	private String composeDto(TypeElement tElem, ToWeb toWeb, Set<String> importSet, Map<String, FieldInfoDto> fieldNamesMap){
		System.out.println("\n*******************composeDto*****************\n");
		final StringBuilder sb = new StringBuilder();

			
		String curPack = toWeb.dtoPackage();
		String currSuf = toWeb.dtoSuffix();

		//pinto el package
		sb.append("package ").append(curPack).append(";\n\n");
		//Pinto los import
		CodeWriterHelper.addImports(sb, importSet);
		//pinto la declaracion de la clase y abro el cuerpo
		sb.append("\n\npublic class ").append( tElem.getSimpleName() + currSuf ).append(" { \n");
		//Pinto campos
		CodeWriterHelper.addFields(sb, fieldNamesMap);
		//pinto getter y setter
		CodeWriterHelper.addGetterAndSetters(sb, fieldNamesMap);
		//cierro el cuerpo de la clase
		sb.append("\n}\n");


		System.out.println(sb.toString());
		return sb.toString();
	}

	private String composeAssembler(TypeElement tElem, ToWeb toWeb, Set<String> importSet,  Map<String, FieldInfoDto> fieldNamesMap){

		System.out.println("\n*******************composeAssembler*****************\n");
		StringBuilder sb = new StringBuilder();
		String curPack = toWeb.assemblerPackage();
		String currSuf = toWeb.assemblerSuffix();

		int maxDepth = toWeb.assemblerDepth();
		//1.-Pinto el package
		sb.append("package ").append(curPack).append(";\n\n");
		//2.-Pinto los imports
		String domainClass = tElem.getSimpleName().toString();
		String dtoClass = domainClass + toWeb.dtoSuffix();
		CodeWriterHelper.addImports(sb, importSet);
		sb.append("\nimport ").append(tElem.toString()).append(";");
		sb.append("\nimport ").append(toWeb.dtoPackage()).append(".").append(dtoClass).append(";");

		//3.-Pinto la definicion de la clase
		sb.append("\n\npublic class ").append(tElem.getSimpleName() + currSuf ).append(" { \n");

		//4.-Pinto el metodo domainToDto
		sb.append("\n\tpublic static ").append(dtoClass).append(" domainToDto(").append(domainClass).append(" domain) {")
			.append("\n\t\treturn domainToDto(domain, null);\n\t}\n");
		sb.append("\n\tpublic static ").append(dtoClass).append(" domainToDto(").append(domainClass).append(" domain, Integer depth) {");
		sb.append("\n\t\tif(domain == null ) {\n\t\t\treturn null;\n\t\t}");
		sb.append("\n\t\t//Modificado2");
		sb.append("\n\t\t").append(dtoClass).append(" dto = new ").append(dtoClass).append("();");
		CodeWriterHelper.addSetFromGet(sb, fieldNamesMap, "domain", "dto", toWeb.dtoSuffix(), currSuf, true);
		sb.append("\n\t\treturn dto;\n\t}\n");
		//5.-Pinto el metodo dtoToDomain
		sb.append("\n\tpublic static ").append(domainClass).append(" dtoToDomain(").append(dtoClass).append(" dto) {")
			.append("\n\t\treturn dtoToDomain(dto, null);\n\t}\n");

		sb.append("\n\tpublic static ").append(domainClass).append(" dtoToDomain(").append(dtoClass).append(" dto, Integer depth) {");
		sb.append("\n\t\tif(dto == null ) {\n\t\t\treturn null;\n\t\t}");
		sb.append("\n\t\t").append(domainClass).append(" domain = new ").append(domainClass).append("();");
		CodeWriterHelper.addSetFromGet(sb, fieldNamesMap, "dto", "domain", toWeb.dtoSuffix(), currSuf, false);
		sb.append("\n\t\treturn domain;\n\t}\n");
		//6.-Cierro la clase
		sb.append("\n}\n");
		System.out.println(sb.toString());
		return sb.toString();
	}

		
	private void composeImportsAndFields(TypeElement tElem, Set<String> importSet, Map<String, FieldInfoDto> fieldNamesMap){
		for(Element e: tElem.getEnclosedElements()){
			if(e instanceof VariableElement){
				VariableElement ve = (VariableElement)e;
				TypeElement type = ProcessorHelper.getTypeElement(processingEnv, ve);
				System.out.println("\n*******************composeImportsAndFields:\n"+type+ "; generic "+ ProcessorHelper.getGenericType(ve)+"\n*****************\n");

				String pack = null;
				ToWeb toWebFe =  type.getAnnotation(ToWeb.class);
				String baseName = type.getSimpleName().toString();
   				String strClass = type.toString();
   				FieldInfoDto info = null;
				if(strClass.equals("java.util.List")){
					pack = "\nimport "+ strClass + ";";
					importSet.add(pack);
					TypeMirror genericType = ProcessorHelper.getGenericType(ve);
   					TypeElement typeGeneric = (TypeElement)((DeclaredType)genericType).asElement();
					String fullName = genericType.toString();
					ToWeb toWebGeneric =  typeGeneric.getAnnotation(ToWeb.class);
					baseName = fullName.substring(fullName.lastIndexOf(".") + 1);
					boolean needAssembler = false;
					if(toWebGeneric != null && !toWebGeneric.dtoPackage().isEmpty()){
						baseName +=  toWebGeneric.dtoSuffix();
						importSet.add("\nimport "+ fullName + ";");
						System.out.println("toWebFe baseName " + baseName);
						pack = "\nimport " + toWebGeneric.dtoPackage() + "." + baseName + ";";
						needAssembler = true;
						System.out.println("toWebFe pack " + pack);
					} else {
						pack = "\nimport " + fullName + ";";
					}
					String listBaseName = "List<" + baseName + ">";
					importSet.add("\nimport java.util.ArrayList;");
					importSet.add("\nimport java.util.List;");
					info = new FieldInfoDto(baseName, listBaseName, "new ArrayList<>();", needAssembler, true);	
				} else if(strClass.equals("java.util.Set")){
					System.out.println("Es una Set");
				}  else if(strClass.equals("java.util.Map")){
					System.out.println("Es una Map");
				} else if(toWebFe != null && !toWebFe.dtoPackage().isEmpty()){
					//campos de clases anotadas con toWebFe
					baseName +=  toWebFe.dtoSuffix();
					pack = "\nimport " + toWebFe.dtoPackage() + "." + baseName + ";";
					info = new FieldInfoDto(baseName, true, false);
				} else {
					//clases no anotadas con ToWeb
					if(!strClass.contains("java.lang.")){
						pack = "\nimport "+ strClass + ";";
					}
					info = new FieldInfoDto(baseName, false, false);
				}
				if(pack != null){
					importSet.add(pack);
				}
				
				fieldNamesMap.put(ve.getSimpleName().toString(), info);
			}
		}
	}

	
	private void fillOriginals(RoundEnvironment roundEnv){
		if(originals == null){
			synchronized(ToWebAnnotationProcessor.class){
				if(originals == null){
					originals = roundEnv.getRootElements().stream().map(Object::toString).collect(Collectors.toSet());
					System.out.println("\n*******************\n"+originals+"\n*****************\n");
				}
			}
		}
		
	}
	private boolean isOriginal(String className){
		return originals.contains(className);
	}
}