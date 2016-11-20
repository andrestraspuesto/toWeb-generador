package andrestraspuesto.generador.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention( RetentionPolicy.SOURCE )
public @interface ToWeb {
	public String dtoPackage() default "";
	public String dtoSuffix() default "Dto";
	public String serviceImplPackage() default "";
	public String serviceImplSuffix() default "ServiceImpl";
	public String serviceInterfacePackage() default "";
	public String serviceInterfaceSuffix() default "ServiceInterface";
	public String controllerPackage() default "";
	public String controllerSuffix() default "Controller";
	public int assemblerDepth() default 1;
	public String assemblerPackage() default "";
	public String assemblerSuffix() default "Assembler";
	public String daoImplSuffix() default "JpaDao";
	public String daoImplPackage() default "";
	public String daoInterfacePre() default "IDao";
	public String daoInterfaceSuffix() default "Interface";
	public String daoInterfacePackage() default "";

}