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

package org.springframework.shell2;

import java.util.List;

import org.springframework.core.MethodParameter;

/**
 * Represents classes that will resolve an object from a list of words and a {@link MethodParameter} definition.
 * <p>
 * The list of words in general represents user input, for example, as part of a parsing process.
 * </p>
 * 
 * @author Eric Bottard
 * @author Camilo Gonzalez
 */
public interface ParameterResolver {

	/**
	 * Indicates if this {@link ParameterResolver} supports the given {@link MethodParameter} definition
	 * 
	 * @param parameter
	 * @return true if the parameter is supported by this resolver, false otherwise.
	 */
	boolean supports(MethodParameter parameter);

	/**
	 * Resolves the object to be used as the argument that corresponds to the given {@link MethodParameter} definition
	 * based on the provided
	 * 
	 * @param methodParameter
	 * @param words
	 * @return
	 * @throws ParameterMissingResolutionException
	 *             if the parameter couldn't be resolved because it was missing from the input words
	 */
	Object resolve(MethodParameter methodParameter, List<String> words);

	ParameterDescription describe(MethodParameter parameter);

	List<String> complete(MethodParameter parameter, CompletionContext context);

}
