package andrestraspuesto.generador.helpers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.VariableElement;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.util.Types;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVariable;

import java.util.List;

public class ProcessorHelper  {
	

	public static TypeElement getTypeElement(ProcessingEnvironment processingEnv, VariableElement ve){
		Types typeUtils = processingEnv.getTypeUtils();
		Element element = typeUtils.asElement(ve.asType());
	    return (element instanceof TypeElement) ? (TypeElement)element : null;

	}

	public static TypeMirror getGenericType(VariableElement field) {
    	TypeMirror genericCanonicalType = null;
    	List<? extends TypeMirror> typeArguments = ((DeclaredType) field.asType()).getTypeArguments();
    	if(!typeArguments.isEmpty()){
            //TODO corregir para que acepte definiciones de genericos con wildcard
        	genericCanonicalType = typeArguments.get(0);
        }
        return genericCanonicalType;
    		
    }

}