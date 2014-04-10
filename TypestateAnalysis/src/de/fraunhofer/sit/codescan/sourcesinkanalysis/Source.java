package de.fraunhofer.sit.codescan.sourcesinkanalysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.LOCAL_VARIABLE,ElementType.FIELD})
public @interface Source {
}
