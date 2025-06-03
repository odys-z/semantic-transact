package io.odysz.semantics.meta;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Semantation {

	/** The field in a meta type doesn't actually exists in the DB table */
	boolean noDBExists() default true;

}
