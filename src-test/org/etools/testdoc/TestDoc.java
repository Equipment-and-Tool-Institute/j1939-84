package org.etools.testdoc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

@Target({ TYPE, METHOD })
public @interface TestDoc {
	String[] dependsOn() default {};

	String description() default "";

	TestItem[] items() default {};
}