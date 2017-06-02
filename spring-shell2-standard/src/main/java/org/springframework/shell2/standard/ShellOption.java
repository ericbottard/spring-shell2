/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.shell2.standard;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to customize handling of a {@link ShellMethod} parameter.
 *
 * @author Eric Bottard
 * @author Florent Biville
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ShellOption {

	String NULL = "__NULL__";

	String NONE = "__NONE__";

	/**
	 * Marker value to indicate that heuristics should be used to derive arity.
	 */
	int ARITY_USE_HEURISTICS = -1;

	/**
	 * The key(s) (without the {@link ShellMethod#prefix()}) by which this parameter can be referenced
	 * when using named parameters. If none is specified, the actual method parameter name will be used.
	 */
	String[] value() default {};

	/**
	 * Return the number of input "words" this parameter consumes. Default is 1, except when parameter type is boolean,
	 * in which case it is 0.
	 */
	int arity() default ARITY_USE_HEURISTICS;

	/**
	 * The textual (pre-conversion) value to assign to this parameter if no value is provided by the user.
	 */
	String defaultValue() default NONE;

	/**
	 * Return a short description of the parameter.
	 */
	String help() default "";

	Class<? extends ValueProvider> valueProvider() default NoValueProvider.class;

	interface NoValueProvider extends ValueProvider {

	}
}
