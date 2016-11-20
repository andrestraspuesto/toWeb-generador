package andrestraspuesto.generador.helpers;

import java.util.Map;
import java.util.Set;

import andrestraspuesto.generador.dto.FieldInfoDto;


public class CodeWriterHelper {

	public static void addImports(StringBuilder sb, Set<String> importSet){
		for(String s: importSet){
			sb.append(s);
		}
	}


	public static void addFields(StringBuilder sb, Map<String, FieldInfoDto> fieldNamesMap){
		for(Map.Entry<String, FieldInfoDto> e: fieldNamesMap.entrySet()){
			FieldInfoDto info = e.getValue();
			String def = info.getFieldType();
			if(info.isCollection()){
				def = info.getCollectionDefinition();
			}
			 sb.append("\n\tprivate ").append(def).append(" ").append(e.getKey()).append(";\n");
		}
	}

	public static void addGetterAndSetters(StringBuilder sb, Map<String, FieldInfoDto> fieldNamesMap){
		for(Map.Entry<String, FieldInfoDto> e: fieldNamesMap.entrySet()){
			FieldInfoDto info = e.getValue();
			String v = info.getFieldType();
			if(info.isCollection()){
				v = info.getCollectionDefinition();
			}
			String k = e.getKey();
			sb.append("\n\tpublic ").append(v).append(" get")
			.append(k.substring(0,1).toUpperCase()).append(k.substring(1)).append("(){ ")
			.append("\n\t\treturn this.").append(k).append(";").append("\n\t}\n")
			.append("\n\tpublic void ").append(" set")
			.append(k.substring(0,1).toUpperCase()).append(k.substring(1)).append("(")
			.append(v).append(" ").append(k).append(") {")
			.append("\n\t\t this.").append(k).append(" = ").append(k).append(";").append("\n\t}\n");
		}
	}

	public static void addSetFromGet(StringBuilder sb, Map<String, FieldInfoDto> fieldNamesMap, String src, String dst, String dtoSuf, String assSuf, boolean toDto){
		for(Map.Entry<String, FieldInfoDto> e: fieldNamesMap.entrySet()){
			FieldInfoDto info = e.getValue();
			if(info != null){
				String k = e.getKey();
				String assembler = "";
				String fieldName = k.substring(0,1).toUpperCase() + k.substring(1);
				String getter = src + ".get" + fieldName + "()";
				String setter = dst + ".set" + fieldName; // Ojo que al setter le faltan parentesis
				if(!info.isCollection() || !info.isNeedAssembler()){
					String v = info.getFieldType();
					if(v.endsWith(dtoSuf)){
						sb.append("\n\t\tif(depth == null || depth >= 0 ){\n\t\t\t");
						assembler = composeAsembler(v, src, dst, dtoSuf, assSuf);
					} else {
						sb.append("\n\t\t");
					}
					sb.append(setter).append("( ");
					if(!assembler.isEmpty()){
						sb.append(assembler);
					}
					sb.append(getter);
					if(!assembler.isEmpty()){
						sb.append(", depth != null? depth - 1: null));\n\t\t}");
					} else {
						sb.append(");\n");
					}
				} else {
					//se trata de una lista de objetos que hay que ensamblar
					composeListAssembling(info, sb, toDto, dtoSuf, getter, setter, src, dst, assSuf);
				}

			}
		}
	}

	private static void composeListAssembling(FieldInfoDto info, StringBuilder sb, boolean toDto, String dtoSuf, 
		String getter, String setter, String src, String dst, String assSuf){
		//Se trata de una lista o Set
		//1.-Creo la instancia que corresponda con new
		sb.append("\n\t\tif("+getter+" != null ){\n\t\t\t");
		String colContenedor = info.getCollectionDefinition();
		String constructor = info.getCollectionConstructor();
		String innerObjSrc = info.getFieldType();
		String innerObj = info.getFieldType();
		String iniAssembler = "";
		String finAssembler = "";
		//2.-Si hay que ensamblar determino la cadena adecuada para llamar al assembler, sino ira vacia
		if(toDto){
			innerObjSrc = innerObjSrc.replace(dtoSuf, "");
		} else {
			System.out.println("colContenedor = " + colContenedor + " dtoSuf =" +dtoSuf);
			colContenedor = colContenedor.replace(dtoSuf,"");
			innerObj = innerObj.replace(dtoSuf, "");
		}
		iniAssembler = composeAsembler(info.getFieldType(), src, dst, dtoSuf, assSuf);
		finAssembler = ")";
		//3.-Destermino si es una lista de objetos que hay que ensamblar
		sb.append("\n\t\t\t").append(colContenedor).append(" auxCol = ").append(constructor);
		//4.-creo un for que recorre la coleccion original metiendo los valores en la nueva
		sb.append("\n\t\t\tfor(").append(innerObjSrc).append(" o: ").append(getter).append("){");
		sb.append("\n\t\t\t\t").append("auxCol.add(").append(iniAssembler).append("o").append(finAssembler).append(");");
		sb.append("\n\t\t\t}");
		//5.-Asigno la lista recien creada al destino
		sb.append("\n\t\t\t").append(setter).append("(auxCol);");
		sb.append("\n\t\t}");
	}

	private static String composeAsembler(String fieldName, String src, String dst, String dtoSuf, String assSuf){
		return fieldName.substring(0,fieldName.length() - dtoSuf.length()) + assSuf + "." + src + "To" 
									+ dst.substring(0,1).toUpperCase() + dst.substring(1) + "(";
	}
}