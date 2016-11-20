package andrestraspuesto.generador.dto;

public final class FieldInfoDto {

	private final String fieldType;

	private final String collectionDefinition;

	private final String collectionConstructor;

	private final boolean needAssembler;

	private final boolean collection;


	public FieldInfoDto(String fieldType, boolean needAssembler, boolean collection){
		this(fieldType, null,  null, needAssembler, collection);		
	}	
	
	public FieldInfoDto(String fieldType, String collectionDefinition,  String collectionConstructor, boolean needAssembler, boolean collection){
		this.fieldType = fieldType;
		this.collectionDefinition = collectionDefinition;
		this.collectionConstructor = collectionConstructor;
		this.needAssembler = needAssembler;
		this.collection = collection;
	}


	public final boolean isCollection(){ return collection;}

	public final boolean isNeedAssembler(){ return needAssembler;}

	public final String getFieldType(){return fieldType;}

	public final String getCollectionDefinition(){return collectionDefinition;}

	public final String getCollectionConstructor(){return collectionConstructor;}

}