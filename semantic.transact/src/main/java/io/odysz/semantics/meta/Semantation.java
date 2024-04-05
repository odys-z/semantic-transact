package io.odysz.semantics.meta;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Semantation {

	boolean noDBExists() default true;

}
