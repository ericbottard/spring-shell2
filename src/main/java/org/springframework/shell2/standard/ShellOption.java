/*
 * Copyright 2015-2017 the original author or authors.
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
 * Used to customize handling of a {@link ShellMethod} parameters.
 *
 * @author Eric Bottard
 * @author Florent Biville
 * @author Camilo Gonzalez
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
public @interface ShellOption {

	String NULL = "__NULL__";

	String NONE = "__NONE__";

	/**
	 * The key(s) (without the {@link ShellMethod#prefix()}) by which this parameter can be referenced when using named
	 * parameters. If none is specified, the actual method parameter name will be used.
	 */
	String[] value() default {};

	/**
	 * Return the number of input "words" this parameter consumes.
	 */
	int arity() default 1;

	/**
	 * The textual (pre-conversion) value to assign to this parameter if no value is provided by the user.
	 */
	String defaultValue() default NONE;

	/**
	 * Return a short description of the parameter.
	 */
	String help() default "";

	/**
	 * @return true if this option will be requested in interactive mode to the user, or if the option is expected as
	 *         part of the command (default false)
	 */
	boolean interactive() default false;

	/**
	 * @return true if this option will be masked in the console (e.g. for passwords). Only valid in conjunction with
	 *         interactive mode. (default false)
	 */
	boolean masked() default false;
}
